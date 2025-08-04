package org.scoula.codef.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.push.dto.feedback.DayOfWeekPeak;
import org.scoula.push.dto.feedback.WeeklySpendingComparison;

import java.util.Map;

public interface FeedbackAnalysisMapper {
    
    /**
     * 사용자의 주간 지출 비교 데이터 조회
     * @param userId 사용자 ID
     * @return 이번 주, 지난 주 지출 금액
     */
    Map<String, Long> getWeeklySpendingComparison(@Param("userId") Long userId);
    
    /**
     * 사용자의 요일별 지출 피크 분석
     * @param userId 사용자 ID
     * @return 가장 많이 지출하는 요일 정보
     */
    DayOfWeekPeak getDayOfWeekPeak(@Param("userId") Long userId);
    
    /**
     * 사용자가 카드를 보유하고 있는지 확인
     * @param userId 사용자 ID
     * @return 카드 보유 여부
     */
    boolean hasCardData(@Param("userId") Long userId);
}