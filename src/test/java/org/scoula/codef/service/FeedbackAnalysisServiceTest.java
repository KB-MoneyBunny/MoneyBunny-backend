package org.scoula.codef.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.codef.mapper.FeedbackAnalysisMapper;
import org.scoula.push.dto.feedback.DayOfWeekPeak;
import org.scoula.push.dto.feedback.WeeklySpendingComparison;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackAnalysisService 단위 테스트")
class FeedbackAnalysisServiceTest {

    @Mock
    private FeedbackAnalysisMapper feedbackAnalysisMapper;

    @InjectMocks
    private FeedbackAnalysisService feedbackAnalysisService;

    private Long userId;
    private DayOfWeekPeak mockDayOfWeekPeak;

    @BeforeEach
    void setUp() {
        userId = 1L;
        
        mockDayOfWeekPeak = DayOfWeekPeak.builder()
                .dayOfWeek(6)
                .dayName("토요일")
                .totalAmount(500000L)
                .transactionCount(15)
                .build();
    }

    // ====================================
    // 주간 지출 비교 분석 테스트
    // ====================================

    @Test
    @DisplayName("주간 지출 비교 분석 - 성공 (지출 증가)")
    void analyzeWeeklySpending_Success_Increase() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("this_week_amount", new BigDecimal("300000"));
        mockResult.put("last_week_amount", new BigDecimal("200000"));
        
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId)).thenReturn(mockResult);

        // When
        WeeklySpendingComparison result = feedbackAnalysisService.analyzeWeeklySpending(userId);

        // Then
        assertNotNull(result);
        assertEquals(300000L, result.getThisWeekAmount());
        assertEquals(200000L, result.getLastWeekAmount());
        assertEquals(50.0, result.getChangePercentage(), 0.01);
        assertTrue(result.getIsIncrease());
        assertEquals("증가", result.getChangeDirection());
        
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
    }

    @Test
    @DisplayName("주간 지출 비교 분석 - 성공 (지출 감소)")
    void analyzeWeeklySpending_Success_Decrease() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("this_week_amount", new BigDecimal("150000"));
        mockResult.put("last_week_amount", new BigDecimal("300000"));
        
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId)).thenReturn(mockResult);

        // When
        WeeklySpendingComparison result = feedbackAnalysisService.analyzeWeeklySpending(userId);

        // Then
        assertNotNull(result);
        assertEquals(150000L, result.getThisWeekAmount());
        assertEquals(300000L, result.getLastWeekAmount());
        assertEquals(50.0, result.getChangePercentage(), 0.01);
        assertFalse(result.getIsIncrease());
        assertEquals("감소", result.getChangeDirection());
        
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
    }

    @Test
    @DisplayName("주간 지출 비교 분석 - 지출 변화 없음")
    void analyzeWeeklySpending_NoChange() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("this_week_amount", new BigDecimal("250000"));
        mockResult.put("last_week_amount", new BigDecimal("250000"));
        
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId)).thenReturn(mockResult);

        // When
        WeeklySpendingComparison result = feedbackAnalysisService.analyzeWeeklySpending(userId);

        // Then
        assertNotNull(result);
        assertEquals(250000L, result.getThisWeekAmount());
        assertEquals(250000L, result.getLastWeekAmount());
        assertEquals(0.0, result.getChangePercentage(), 0.01);
        assertFalse(result.getIsIncrease());
        assertEquals("변화 없음", result.getChangeDirection());
        
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
    }

    @Test
    @DisplayName("주간 지출 비교 분석 - null 값 처리")
    void analyzeWeeklySpending_NullValues() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("this_week_amount", null);
        mockResult.put("last_week_amount", null);
        
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId)).thenReturn(mockResult);

        // When
        WeeklySpendingComparison result = feedbackAnalysisService.analyzeWeeklySpending(userId);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getThisWeekAmount());
        assertEquals(0L, result.getLastWeekAmount());
        assertEquals(0.0, result.getChangePercentage(), 0.01);
        assertFalse(result.getIsIncrease());
        assertEquals("변화 없음", result.getChangeDirection());
        
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
    }

    @Test
    @DisplayName("주간 지출 비교 분석 - 지난주 데이터 없음 (0)")
    void analyzeWeeklySpending_LastWeekZero() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("this_week_amount", new BigDecimal("100000"));
        mockResult.put("last_week_amount", new BigDecimal("0"));
        
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId)).thenReturn(mockResult);

        // When
        WeeklySpendingComparison result = feedbackAnalysisService.analyzeWeeklySpending(userId);

        // Then
        assertNotNull(result);
        assertEquals(100000L, result.getThisWeekAmount());
        assertEquals(0L, result.getLastWeekAmount());
        // 지난주가 0일 때는 변화율 0%로 처리됨 (WeeklySpendingComparison.of 구현 확인)
        assertEquals(0.0, result.getChangePercentage(), 0.01);
        assertFalse(result.getIsIncrease());
        assertEquals("변화 없음", result.getChangeDirection());
        
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
    }

    @Test
    @DisplayName("주간 지출 비교 분석 - 매퍼 예외 처리")
    void analyzeWeeklySpending_MapperException() {
        // Given
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId))
                .thenThrow(new RuntimeException("Database connection error"));

        // When
        WeeklySpendingComparison result = feedbackAnalysisService.analyzeWeeklySpending(userId);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getThisWeekAmount());
        assertEquals(0L, result.getLastWeekAmount());
        assertEquals(0.0, result.getChangePercentage(), 0.01);
        assertFalse(result.getIsIncrease());
        
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
    }

    // ====================================
    // 요일별 지출 피크 분석 테스트
    // ====================================

    @Test
    @DisplayName("요일별 지출 피크 분석 - 성공")
    void analyzeDayOfWeekPeak_Success() {
        // Given
        when(feedbackAnalysisMapper.getDayOfWeekPeak(userId)).thenReturn(mockDayOfWeekPeak);

        // When
        DayOfWeekPeak result = feedbackAnalysisService.analyzeDayOfWeekPeak(userId);

        // Then
        assertNotNull(result);
        assertEquals(6, result.getDayOfWeek());
        assertEquals("토요일", result.getDayName());
        assertEquals(500000L, result.getTotalAmount());
        assertEquals(15, result.getTransactionCount());
        
        verify(feedbackAnalysisMapper).getDayOfWeekPeak(userId);
    }

    @Test
    @DisplayName("요일별 지출 피크 분석 - 데이터 없음")
    void analyzeDayOfWeekPeak_NoData() {
        // Given
        when(feedbackAnalysisMapper.getDayOfWeekPeak(userId)).thenReturn(null);

        // When
        DayOfWeekPeak result = feedbackAnalysisService.analyzeDayOfWeekPeak(userId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getDayOfWeek());
        assertEquals("데이터 없음", result.getDayName());
        assertEquals(0L, result.getTotalAmount());
        assertEquals(0, result.getTransactionCount());
        
        verify(feedbackAnalysisMapper).getDayOfWeekPeak(userId);
    }

    @Test
    @DisplayName("요일별 지출 피크 분석 - 매퍼 예외 처리")
    void analyzeDayOfWeekPeak_MapperException() {
        // Given
        when(feedbackAnalysisMapper.getDayOfWeekPeak(userId))
                .thenThrow(new RuntimeException("Database query failed"));

        // When
        DayOfWeekPeak result = feedbackAnalysisService.analyzeDayOfWeekPeak(userId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getDayOfWeek());
        assertEquals("분석 실패", result.getDayName());
        assertEquals(0L, result.getTotalAmount());
        assertEquals(0, result.getTransactionCount());
        
        verify(feedbackAnalysisMapper).getDayOfWeekPeak(userId);
    }

    @Test
    @DisplayName("요일별 지출 피크 분석 - 일요일 피크")
    void analyzeDayOfWeekPeak_SundayPeak() {
        // Given
        DayOfWeekPeak sundayPeak = DayOfWeekPeak.builder()
                .dayOfWeek(1)
                .dayName("일요일")
                .totalAmount(800000L)
                .transactionCount(20)
                .build();
        
        when(feedbackAnalysisMapper.getDayOfWeekPeak(userId)).thenReturn(sundayPeak);

        // When
        DayOfWeekPeak result = feedbackAnalysisService.analyzeDayOfWeekPeak(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getDayOfWeek());
        assertEquals("일요일", result.getDayName());
        assertEquals(800000L, result.getTotalAmount());
        assertEquals(20, result.getTransactionCount());
        
        verify(feedbackAnalysisMapper).getDayOfWeekPeak(userId);
    }

    // ====================================
    // 카드 데이터 보유 여부 테스트
    // ====================================

    @Test
    @DisplayName("카드 데이터 보유 여부 - 데이터 있음")
    void hasCardData_True() {
        // Given
        when(feedbackAnalysisMapper.hasCardData(userId)).thenReturn(true);

        // When
        boolean result = feedbackAnalysisService.hasCardData(userId);

        // Then
        assertTrue(result);
        verify(feedbackAnalysisMapper).hasCardData(userId);
    }

    @Test
    @DisplayName("카드 데이터 보유 여부 - 데이터 없음")
    void hasCardData_False() {
        // Given
        when(feedbackAnalysisMapper.hasCardData(userId)).thenReturn(false);

        // When
        boolean result = feedbackAnalysisService.hasCardData(userId);

        // Then
        assertFalse(result);
        verify(feedbackAnalysisMapper).hasCardData(userId);
    }

    @Test
    @DisplayName("카드 데이터 보유 여부 - 매퍼 예외 처리")
    void hasCardData_MapperException() {
        // Given
        when(feedbackAnalysisMapper.hasCardData(userId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        boolean result = feedbackAnalysisService.hasCardData(userId);

        // Then
        assertFalse(result); // 예외 발생시 false 반환
        verify(feedbackAnalysisMapper).hasCardData(userId);
    }

    // ====================================
    // 통합 테스트
    // ====================================

    @Test
    @DisplayName("통합 테스트 - 모든 분석 메서드 연속 호출")
    void integrationTest_AllAnalysisMethods() {
        // Given
        Map<String, Object> weeklyResult = new HashMap<>();
        weeklyResult.put("this_week_amount", new BigDecimal("400000"));
        weeklyResult.put("last_week_amount", new BigDecimal("350000"));
        
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId)).thenReturn(weeklyResult);
        when(feedbackAnalysisMapper.getDayOfWeekPeak(userId)).thenReturn(mockDayOfWeekPeak);
        when(feedbackAnalysisMapper.hasCardData(userId)).thenReturn(true);

        // When
        WeeklySpendingComparison weeklyComparison = feedbackAnalysisService.analyzeWeeklySpending(userId);
        DayOfWeekPeak dayPeak = feedbackAnalysisService.analyzeDayOfWeekPeak(userId);
        boolean hasData = feedbackAnalysisService.hasCardData(userId);

        // Then
        // 주간 지출 분석 결과 검증
        assertNotNull(weeklyComparison);
        assertEquals(400000L, weeklyComparison.getThisWeekAmount());
        assertEquals(350000L, weeklyComparison.getLastWeekAmount());
        assertTrue(weeklyComparison.getIsIncrease());
        
        // 요일별 피크 분석 결과 검증
        assertNotNull(dayPeak);
        assertEquals("토요일", dayPeak.getDayName());
        assertEquals(500000L, dayPeak.getTotalAmount());
        
        // 카드 데이터 보유 여부 검증
        assertTrue(hasData);
        
        // 모든 매퍼 메서드가 호출되었는지 확인
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
        verify(feedbackAnalysisMapper).getDayOfWeekPeak(userId);
        verify(feedbackAnalysisMapper).hasCardData(userId);
    }

    @Test
    @DisplayName("BigDecimal 변환 테스트 - 다양한 값")
    void testBigDecimalConversion() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("this_week_amount", new BigDecimal("123456.789")); // 소수점 있는 값
        mockResult.put("last_week_amount", new BigDecimal("987654"));      // 정수 값
        
        when(feedbackAnalysisMapper.getWeeklySpendingComparison(userId)).thenReturn(mockResult);

        // When
        WeeklySpendingComparison result = feedbackAnalysisService.analyzeWeeklySpending(userId);

        // Then
        assertNotNull(result);
        assertEquals(123456L, result.getThisWeekAmount()); // 소수점 버림 확인
        assertEquals(987654L, result.getLastWeekAmount());
        
        verify(feedbackAnalysisMapper).getWeeklySpendingComparison(userId);
    }
}