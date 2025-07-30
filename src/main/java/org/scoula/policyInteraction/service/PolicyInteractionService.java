package org.scoula.policyInteraction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.domain.PolicyVectorVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.UserVectorVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.BookmarkWithPolicyDTO;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.scoula.policyInteraction.util.UserVectorUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyInteractionService {
    
    private final PolicyInteractionMapper policyInteractionMapper;
    private final PolicyMapper policyMapper;
    
    // ────────────────────────────────────────
    // 📌 북마크 관련
    // ────────────────────────────────────────
    
    /** 북마크 추가 */
    @Transactional
    public boolean addBookmark(Long userId, Long policyId) {
        // 이미 북마크 되어있는지 확인
        YouthPolicyBookmarkVO existing = policyInteractionMapper.selectBookmark(userId, policyId);
        if (existing != null) {
            log.info("이미 북마크된 정책입니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        YouthPolicyBookmarkVO bookmark = YouthPolicyBookmarkVO.builder()
                .userId(userId)
                .policyId(policyId)
                .build();
                
        int result = policyInteractionMapper.insertBookmark(bookmark);
        
        // 북마크 성공 시 사용자 벡터 갱신 (가중치: 0.3)
        if (result > 0) {
            updateUserVectorWithLinearInterpolation(userId, policyId, 0.3);
        }
        
        return result > 0;
    }
    
    /** 북마크 삭제 */
    @Transactional
    public boolean removeBookmark(Long userId, Long policyId) {
        int result = policyInteractionMapper.deleteBookmark(userId, policyId);
        return result > 0;
    }
    
    /** 사용자의 북마크 목록 조회 (정책 정보 포함) */
    public List<BookmarkWithPolicyDTO> getUserBookmarks(Long userId) {
        return policyInteractionMapper.selectBookmarksByUserId(userId);
    }
    
    // ────────────────────────────────────────
    // 📌 신청 관련
    // ────────────────────────────────────────
    
    /** 정책 신청 등록 */
    @Transactional
    public boolean addApplication(Long userId, Long policyId) {
        // 이미 신청했는지 확인
        UserPolicyApplicationVO existing = policyInteractionMapper.selectApplication(userId, policyId);
        if (existing != null) {
            log.info("이미 신청한 정책입니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        UserPolicyApplicationVO application = UserPolicyApplicationVO.builder()
                .userId(userId)
                .policyId(policyId)
                .applicationUrl(null)  // 선택적 필드로 null 처리
                .build();
                
        int result = policyInteractionMapper.insertApplication(application);
        
        // 신청 성공 시 사용자 벡터 갱신 (가중치: 0.7)
        if (result > 0) {
            updateUserVectorWithLinearInterpolation(userId, policyId, 0.7);
        }
        
        return result > 0;
    }
    
    /** 사용자의 신청 목록 조회 (정책 정보 포함) */
    public List<ApplicationWithPolicyDTO> getUserApplications(Long userId) {
        return policyInteractionMapper.selectApplicationsByUserId(userId);
    }
    
    // ────────────────────────────────────────
    // 📌 사용자 벡터 갱신 관련
    // ────────────────────────────────────────
    
    /**
     * 선형보간법을 사용하여 사용자 벡터 갱신
     * 공식: newVector = (1 - t) * currentVector + t * policyVector
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @param weight 행동 가중치 (조회: 0.1, 북마크: 0.3, 신청: 0.7)
     */
    private void updateUserVectorWithLinearInterpolation(Long userId, Long policyId, double weight) {
        try {
            // 1. 정책 벡터 조회
            PolicyVectorVO policyVector = policyMapper.findByPolicyId(policyId);
            if (policyVector == null) {
                log.warn("[사용자 벡터] 정책 벡터를 찾을 수 없음 - 정책 ID: {}", policyId);
                return;
            }
            
            // 2. 사용자 벡터 조회 또는 초기화
            UserVectorVO userVector = policyInteractionMapper.findByUserId(userId);
            if (userVector == null) {
                userVector = UserVectorUtil.createInitialUserVector(userId);
                log.info("[사용자 벡터] 초기 벡터 생성 - userId: {}", userId);
            }
            
            // 3. 선형보간법 적용
            // t는 가중치 * 학습률
            double t = weight * 0.1; // 학습률 0.1 적용
            applyLinearInterpolation(userVector, policyVector, t);
            
            // 4. DB 저장
            if (userVector.getId() == null) {
                policyInteractionMapper.insertUserVector(userVector);
                log.info("[사용자 벡터] 신규 생성 완료 - userId: {}", userId);
            } else {
                policyInteractionMapper.updateUserVector(userVector);
                log.info("[사용자 벡터] 업데이트 완료 - userId: {}", userId);
            }
            
        } catch (Exception e) {
            log.error("[사용자 벡터] 갱신 실패 - userId: {}, policyId: {}, 오류: {}", 
                    userId, policyId, e.getMessage());
        }
    }
    
    /**
     * 선형보간법 적용: result = (1 - t) * userVector + t * policyVector
     * @param userVector 갱신할 사용자 벡터
     * @param policyVector 참조할 정책 벡터
     * @param t 보간 계수 (weight * learningRate)
     */
    private void applyLinearInterpolation(UserVectorVO userVector, PolicyVectorVO policyVector, double t) {
        BigDecimal oneMinusT = BigDecimal.valueOf(1 - t);
        BigDecimal tValue = BigDecimal.valueOf(t);
        
        // 혜택 금액 차원
        BigDecimal newBenefit = userVector.getVecBenefitAmount()
            .multiply(oneMinusT)
            .add(policyVector.getVecBenefitAmount().multiply(tValue))
            .setScale(4, RoundingMode.HALF_UP);
        
        // 마감일 차원
        BigDecimal newDeadline = userVector.getVecDeadline()
            .multiply(oneMinusT)
            .add(policyVector.getVecDeadline().multiply(tValue))
            .setScale(4, RoundingMode.HALF_UP);
        
        // 조회수 차원
        BigDecimal newViews = userVector.getVecViews()
            .multiply(oneMinusT)
            .add(policyVector.getVecViews().multiply(tValue))
            .setScale(4, RoundingMode.HALF_UP);
        
        userVector.setVecBenefitAmount(newBenefit);
        userVector.setVecDeadline(newDeadline);
        userVector.setVecViews(newViews);
        
        log.debug("[선형보간] t: {}, 결과: [{}, {}, {}]", 
            t, newBenefit, newDeadline, newViews);
    }
    
}
