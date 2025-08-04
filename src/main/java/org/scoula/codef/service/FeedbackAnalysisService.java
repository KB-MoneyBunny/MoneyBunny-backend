package org.scoula.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.codef.mapper.FeedbackAnalysisMapper;
import org.scoula.push.dto.feedback.DayOfWeekPeak;
import org.scoula.push.dto.feedback.WeeklySpendingComparison;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackAnalysisService {
    
    private final FeedbackAnalysisMapper feedbackAnalysisMapper;
    
    /**
     * 사용자의 주간 지출 비교 분석
     * @param userId 사용자 ID
     * @return 주간 지출 비교 데이터
     */
    public WeeklySpendingComparison analyzeWeeklySpending(Long userId) {
        log.debug("[피드백분석] 주간 지출 비교 분석 시작: userId={}", userId);
        
        try {
            Map<String, Long> result = feedbackAnalysisMapper.getWeeklySpendingComparison(userId);
            
            Long thisWeek = result.get("thisWeekAmount");
            Long lastWeek = result.get("lastWeekAmount");
            
            log.debug("[피드백분석] 주간 지출 데이터: 이번주={}, 지난주={}", thisWeek, lastWeek);
            
            WeeklySpendingComparison comparison = WeeklySpendingComparison.of(thisWeek, lastWeek);
            
            log.info("[피드백분석] 주간 지출 비교 완료: userId={}, 변화율={}%, 방향={}", 
                    userId, comparison.getChangePercentage(), comparison.getChangeDirection());
            
            return comparison;
            
        } catch (Exception e) {
            log.error("[피드백분석] 주간 지출 비교 분석 실패: userId={}, error={}", userId, e.getMessage(), e);
            return WeeklySpendingComparison.builder()
                    .thisWeekAmount(0L)
                    .lastWeekAmount(0L)
                    .changePercentage(0.0)
                    .isIncrease(false)
                    .build();
        }
    }
    
    /**
     * 사용자의 요일별 지출 피크 분석
     * @param userId 사용자 ID
     * @return 요일별 지출 피크 데이터
     */
    public DayOfWeekPeak analyzeDayOfWeekPeak(Long userId) {
        log.debug("[피드백분석] 요일별 지출 피크 분석 시작: userId={}", userId);
        
        try {
            DayOfWeekPeak peak = feedbackAnalysisMapper.getDayOfWeekPeak(userId);
            
            if (peak == null) {
                log.warn("[피드백분석] 요일별 지출 데이터 없음: userId={}", userId);
                return DayOfWeekPeak.builder()
                        .dayOfWeek(0)
                        .dayName("데이터 없음")
                        .totalAmount(0L)
                        .transactionCount(0)
                        .build();
            }
            
            log.info("[피드백분석] 요일별 지출 피크 완료: userId={}, 피크요일={}, 금액={}", 
                    userId, peak.getDayName(), peak.getTotalAmount());
            
            return peak;
            
        } catch (Exception e) {
            log.error("[피드백분석] 요일별 지출 피크 분석 실패: userId={}, error={}", userId, e.getMessage(), e);
            return DayOfWeekPeak.builder()
                    .dayOfWeek(0)
                    .dayName("분석 실패")
                    .totalAmount(0L)
                    .transactionCount(0)
                    .build();
        }
    }
    
    /**
     * 사용자의 카드 데이터 보유 여부 확인
     * @param userId 사용자 ID
     * @return 카드 데이터 보유 여부
     */
    public boolean hasCardData(Long userId) {
        try {
            boolean hasData = feedbackAnalysisMapper.hasCardData(userId);
            log.debug("[피드백분석] 카드 데이터 보유 여부: userId={}, hasData={}", userId, hasData);
            return hasData;
        } catch (Exception e) {
            log.error("[피드백분석] 카드 데이터 확인 실패: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }
}