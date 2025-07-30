package org.scoula.policyInteraction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.policy.domain.PolicyVectorVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.policyInteraction.domain.UserVectorVO;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.scoula.policyInteraction.util.UserVectorUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 사용자 벡터 배치 갱신 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVectorBatchService {

    private final RedisUtil redisUtil;
    private final PolicyInteractionMapper policyInteractionMapper;
    private final PolicyMapper policyMapper;
    
    // EMA 학습률 (alpha)
    private static final double ALPHA = 0.1;
    
    // 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 사용자 벡터의 3차원 전체를 EMA 방식으로 갱신
     * 조회 행동이 모든 차원의 선호도에 영향을 준다고 가정
     */
    public void updateUserVectorsByEMA() {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        
        try {
            Set<String> dailyViewKeys = redisUtil.getAllDailyViewKeys(yesterday);
            if (dailyViewKeys.isEmpty()) {
                log.info("갱신할 조회 데이터 없음 - 날짜: {}", yesterday);
                return;
            }
            
            // 사용자별로 그룹화하여 처리
            Map<Long, List<String>> userViewData = groupByUser(dailyViewKeys);
            int processedUsers = 0;
            int totalUpdatedVectors = 0;
            
            for (Map.Entry<Long, List<String>> entry : userViewData.entrySet()) {
                Long userId = entry.getKey();
                List<String> userKeys = entry.getValue();
                
                int updatedVectors = processUserViewsDimensionUpdate(userId, userKeys, yesterday);
                totalUpdatedVectors += updatedVectors;
                processedUsers++;
                
                // Redis 데이터 정리
                redisUtil.deleteDailyViewData(userId, yesterday);
            }
            
            log.info("3차원 벡터 갱신 완료 - 처리 사용자: {}, 갱신 벡터: {}", 
                    processedUsers, totalUpdatedVectors);
                    
        } catch (Exception e) {
            log.error("3차원 벡터 갱신 중 오류 발생", e);
            throw e;
        }
    }
    
    /**
     * Redis 키를 사용자별로 그룹화
     */
    private Map<Long, List<String>> groupByUser(Set<String> keys) {
        return keys.stream()
                .collect(Collectors.groupingBy(key -> {
                    Long[] ids = redisUtil.extractIdsFromKey(key);
                    return ids != null ? ids[0] : -1L;
                }))
                .entrySet().stream()
                .filter(entry -> entry.getKey() != -1L)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * 개별 사용자의 3차원 벡터 전체 갱신 처리
     */
    private int processUserViewsDimensionUpdate(Long userId, List<String> userKeys, String date) {
        int updatedCount = 0;
        
        try {
            // 현재 사용자 벡터 조회 또는 초기화
            UserVectorVO userVector = policyInteractionMapper.findByUserId(userId);
            if (userVector == null) {
                userVector = UserVectorUtil.createInitialUserVector(userId);
                policyInteractionMapper.insertUserVector(userVector);
                log.debug("초기 사용자 벡터 생성 - userId: {}", userId);
            }
            
            // 각 정책별로 3차원 벡터 전체 갱신
            for (String key : userKeys) {
                Long[] ids = redisUtil.extractIdsFromKey(key);
                if (ids != null && ids.length >= 2) {
                    Long policyId = ids[1];
                    Long viewCount = redisUtil.getDailyViewCount(userId, policyId, date);
                    
                    if (updateAllDimensionsForPolicy(userVector, policyId, viewCount.intValue())) {
                        updatedCount++;
                    }
                }
            }
            
            // DB 업데이트
            if (updatedCount > 0) {
                policyInteractionMapper.updateUserVector(userVector);
                log.debug("사용자 벡터 3차원 전체 DB 업데이트 완료 - userId: {}, 갱신 정책 수: {}", userId, updatedCount);
            }
            
        } catch (Exception e) {
            log.error("사용자 3차원 벡터 갱신 실패 - userId: {}, 오류: {}", userId, e.getMessage());
        }
        
        return updatedCount;
    }
    
    /**
     * 특정 정책에 대한 3차원 전체 EMA 갱신 (vecBenefitAmount, vecDeadline, vecViews 모두 업데이트)
     * 조회 행동도 사용자의 정책 선호도를 나타내므로 모든 차원에 영향을 줌
     */
    private boolean updateAllDimensionsForPolicy(UserVectorVO userVector, Long policyId, int viewCount) {
        try {
            // 정책 벡터 조회
            PolicyVectorVO policyVector = policyMapper.findByPolicyId(policyId);
            if (policyVector == null) {
                log.warn("정책 벡터를 찾을 수 없음 - policyId: {}", policyId);
                return false;
            }
            
            // EMA 수식: cumulativeWeight = 1 - (1-α)^n
            double cumulativeWeight = 1 - Math.pow(1 - ALPHA, viewCount);
            BigDecimal oneMinusWeight = BigDecimal.valueOf(1 - cumulativeWeight);
            BigDecimal weight = BigDecimal.valueOf(cumulativeWeight);
            
            // 3차원 전체 EMA 갱신
            // 혜택 금액 차원 갱신
            BigDecimal newBenefit = userVector.getVecBenefitAmount()
                .multiply(oneMinusWeight)
                .add(policyVector.getVecBenefitAmount().multiply(weight))
                .setScale(4, RoundingMode.HALF_UP);
            
            // 마감일 차원 갱신
            BigDecimal newDeadline = userVector.getVecDeadline()
                .multiply(oneMinusWeight)
                .add(policyVector.getVecDeadline().multiply(weight))
                .setScale(4, RoundingMode.HALF_UP);
            
            // 조회수 차원 갱신
            BigDecimal newViews = userVector.getVecViews()
                .multiply(oneMinusWeight)
                .add(policyVector.getVecViews().multiply(weight))
                .setScale(4, RoundingMode.HALF_UP);
            
            // 사용자 벡터 업데이트
            userVector.setVecBenefitAmount(newBenefit);
            userVector.setVecDeadline(newDeadline);
            userVector.setVecViews(newViews);
            
            log.trace("EMA 3차원 전체 갱신 - userId: {}, policyId: {}, viewCount: {}, weight: {:.4f}, 결과: [{}, {}, {}]", 
                    userVector.getUserId(), policyId, viewCount, cumulativeWeight, newBenefit, newDeadline, newViews);
            
            return true;
            
        } catch (Exception e) {
            log.error("3차원 전체 갱신 실패 - policyId: {}, 오류: {}", policyId, e.getMessage());
            return false;
        }
    }
}