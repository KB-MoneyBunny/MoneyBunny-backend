package org.scoula.external.gpt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.external.gpt.domain.*;
import org.scoula.external.gpt.mapper.PromptConditionMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromptBuilderService 단위 테스트")
class PromptBuilderServiceTest {

    @Mock
    private PromptConditionMapper promptConditionMapper;

    @InjectMocks
    private PromptBuilderService promptBuilderService;

    private String sampleSupportContent;
    private List<PromptConditionVO> mockPositiveConditions;
    private List<PromptConditionVO> mockNegativeConditions;
    private List<PromptCalculationRuleVO> mockCalculationRules;
    private List<PromptExampleVO> mockPositiveExamples;
    private List<PromptExampleVO> mockNegativeExamples;

    @BeforeEach
    void setUp() {
        sampleSupportContent = "청년 취업자에게 월 30만원의 교통비를 3개월간 지원합니다.";

        // Mock PromptConditionVO 생성
        PromptConditionVO positiveCondition1 = new PromptConditionVO();
        positiveCondition1.setId(1L);
        positiveCondition1.setConditionText("금전적 지원이 명시된 경우");
        positiveCondition1.setType(PromptConditionType.POSITIVE);

        PromptConditionVO positiveCondition2 = new PromptConditionVO();
        positiveCondition2.setId(2L);
        positiveCondition2.setConditionText("구체적인 금액이 제시된 경우");
        positiveCondition2.setType(PromptConditionType.POSITIVE);

        mockPositiveConditions = Arrays.asList(positiveCondition1, positiveCondition2);

        PromptConditionVO negativeCondition1 = new PromptConditionVO();
        negativeCondition1.setId(3L);
        negativeCondition1.setConditionText("교육, 멘토링만 제공하는 경우");
        negativeCondition1.setType(PromptConditionType.NEGATIVE);

        mockNegativeConditions = Arrays.asList(negativeCondition1);

        // Mock PromptCalculationRuleVO 생성
        PromptCalculationRuleVO calculationRule1 = new PromptCalculationRuleVO();
        calculationRule1.setId(1L);
        calculationRule1.setRuleText("월 금액이 명시된 경우, 해당 금액을 그대로 사용");

        PromptCalculationRuleVO calculationRule2 = new PromptCalculationRuleVO();
        calculationRule2.setId(2L);
        calculationRule2.setRuleText("연간 금액인 경우 12로 나누어 월 평균 계산");

        mockCalculationRules = Arrays.asList(calculationRule1, calculationRule2);

        // Mock PromptExampleVO 생성
        PromptExampleVO positiveExample1 = new PromptExampleVO();
        positiveExample1.setId(1L);
        positiveExample1.setExampleText("월 50만원 지원");
        positiveExample1.setType(PromptConditionType.POSITIVE);

        PromptExampleVO positiveExample2 = new PromptExampleVO();
        positiveExample2.setId(2L);
        positiveExample2.setExampleText("교통비 지급");
        positiveExample2.setType(PromptConditionType.POSITIVE);

        mockPositiveExamples = Arrays.asList(positiveExample1, positiveExample2);

        PromptExampleVO negativeExample1 = new PromptExampleVO();
        negativeExample1.setId(3L);
        negativeExample1.setExampleText("창업교육 제공");
        negativeExample1.setType(PromptConditionType.NEGATIVE);

        mockNegativeExamples = Arrays.asList(negativeExample1);
    }

    // ====================================
    // 프롬프트 생성 테스트 - 정상 케이스
    // ====================================

    @Test
    @DisplayName("프롬프트 생성 - 모든 조건 포함")
    void buildPromptOptimized_Success_AllConditions() {
        // Given
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveConditions);
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeConditions);
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(mockCalculationRules);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveExamples);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeExamples);

        // When
        String result = promptBuilderService.buildPromptOptimized(sampleSupportContent);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // 기본 헤더 확인
        assertTrue(result.contains("다음은 청년 정책의 '지원내용'이다."));
        assertTrue(result.contains("지원내용: " + sampleSupportContent));

        // POSITIVE 조건들 확인
        assertTrue(result.contains("이 정책이 다음 조건에 해당하는 경우 isFinancialSupport를 true로 판단한다"));
        assertTrue(result.contains("금전적 지원이 명시된 경우"));
        assertTrue(result.contains("구체적인 금액이 제시된 경우"));

        // NEGATIVE 조건들 확인
        assertTrue(result.contains("다음 조건에 해당하는 경우 false로 판단한다"));
        assertTrue(result.contains("교육, 멘토링만 제공하는 경우"));

        // 계산 규칙 확인
        assertTrue(result.contains("추정 가능한 경우 estimatedAmount를 원 단위 정수로 계산한다"));
        assertTrue(result.contains("월 금액이 명시된 경우, 해당 금액을 그대로 사용"));
        assertTrue(result.contains("연간 금액인 경우 12로 나누어 월 평균 계산"));

        // 예시 확인
        assertTrue(result.contains("월 50만원 지원"));
        assertTrue(result.contains("교통비 지급"));
        assertTrue(result.contains("창업교육 제공"));

        // JSON 형식 확인
        assertTrue(result.contains("결과는 다음 JSON 형식으로 정확히 반환한다"));
        assertTrue(result.contains("\"isFinancialSupport\": true"));
        assertTrue(result.contains("\"estimatedAmount\": 0"));
        assertTrue(result.contains("\"policyBenefitDescription\""));

        // 모든 매퍼 메서드가 호출되었는지 확인
        verify(promptConditionMapper).findConditionsByType(PromptConditionType.POSITIVE);
        verify(promptConditionMapper).findConditionsByType(PromptConditionType.NEGATIVE);
        verify(promptConditionMapper).findAllCalculationRules();
        verify(promptConditionMapper).findExamplesByType(PromptConditionType.POSITIVE);
        verify(promptConditionMapper).findExamplesByType(PromptConditionType.NEGATIVE);
    }

    @Test
    @DisplayName("프롬프트 생성 - 짧은 지원내용")
    void buildPromptOptimized_ShortSupportContent() {
        // Given
        String shortContent = "지원금 지급";
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveConditions);
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeConditions);
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(mockCalculationRules);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveExamples);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeExamples);

        // When
        String result = promptBuilderService.buildPromptOptimized(shortContent);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("지원내용: " + shortContent));
        assertTrue(result.contains("금전적 지원이 명시된 경우"));
    }

    // ====================================
    // 프롬프트 생성 테스트 - 빈 데이터 처리
    // ====================================

    @Test
    @DisplayName("프롬프트 생성 - 빈 조건 목록")
    void buildPromptOptimized_EmptyConditions() {
        // Given
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(Collections.emptyList());
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(Collections.emptyList());
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(Collections.emptyList());
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(Collections.emptyList());
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(Collections.emptyList());

        // When
        String result = promptBuilderService.buildPromptOptimized(sampleSupportContent);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // 기본 구조는 유지되어야 함
        assertTrue(result.contains("다음은 청년 정책의 '지원내용'이다."));
        assertTrue(result.contains("지원내용: " + sampleSupportContent));
        assertTrue(result.contains("결과는 다음 JSON 형식으로 정확히 반환한다"));

        verify(promptConditionMapper).findConditionsByType(PromptConditionType.POSITIVE);
        verify(promptConditionMapper).findConditionsByType(PromptConditionType.NEGATIVE);
        verify(promptConditionMapper).findAllCalculationRules();
        verify(promptConditionMapper).findExamplesByType(PromptConditionType.POSITIVE);
        verify(promptConditionMapper).findExamplesByType(PromptConditionType.NEGATIVE);
    }

    @Test
    @DisplayName("프롬프트 생성 - null 지원내용")
    void buildPromptOptimized_NullSupportContent() {
        // Given
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveConditions);
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeConditions);
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(mockCalculationRules);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveExamples);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeExamples);

        // When
        String result = promptBuilderService.buildPromptOptimized(null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("지원내용: null"));
        assertTrue(result.contains("금전적 지원이 명시된 경우"));
    }

    @Test
    @DisplayName("프롬프트 생성 - 빈 문자열 지원내용")
    void buildPromptOptimized_EmptySupportContent() {
        // Given
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveConditions);
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeConditions);
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(mockCalculationRules);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveExamples);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeExamples);

        // When
        String result = promptBuilderService.buildPromptOptimized("");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("지원내용: "));
        assertTrue(result.contains("금전적 지원이 명시된 경우"));
    }

    // ====================================
    // 프롬프트 생성 테스트 - 예외 처리
    // ====================================

    @Test
    @DisplayName("프롬프트 생성 - 매퍼 예외 처리")
    void buildPromptOptimized_MapperException() {
        // Given
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            promptBuilderService.buildPromptOptimized(sampleSupportContent);
        });

        verify(promptConditionMapper).findConditionsByType(PromptConditionType.POSITIVE);
        // 다른 매퍼 메서드들은 호출되지 않아야 함
        verify(promptConditionMapper, never()).findConditionsByType(PromptConditionType.NEGATIVE);
    }

    // ====================================
    // 프롬프트 구조 검증 테스트
    // ====================================

    @Test
    @DisplayName("프롬프트 구조 검증 - 섹션 순서")
    void verifyPromptStructure() {
        // Given
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveConditions);
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeConditions);
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(mockCalculationRules);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveExamples);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeExamples);

        // When
        String result = promptBuilderService.buildPromptOptimized(sampleSupportContent);

        // Then
        // 각 섹션의 순서 확인
        int headerIndex = result.indexOf("다음은 청년 정책의 '지원내용'이다.");
        int positiveIndex = result.indexOf("이 정책이 다음 조건에 해당하는 경우 isFinancialSupport를 true로 판단한다");
        int negativeIndex = result.indexOf("다음 조건에 해당하는 경우 false로 판단한다");
        int calculationIndex = result.indexOf("추정 가능한 경우 estimatedAmount를 원 단위 정수로 계산한다");
        int descriptionIndex = result.indexOf("지원 형태를 policyBenefitDescription에 간단히 서술한다");
        int jsonIndex = result.indexOf("결과는 다음 JSON 형식으로 정확히 반환한다");

        assertTrue(headerIndex < positiveIndex);
        assertTrue(positiveIndex < negativeIndex);
        assertTrue(negativeIndex < calculationIndex);
        assertTrue(calculationIndex < descriptionIndex);
        assertTrue(descriptionIndex < jsonIndex);
    }

    @Test
    @DisplayName("예시 텍스트 포맷 검증")
    void verifyExampleFormat() {
        // Given
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveConditions);
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeConditions);
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(mockCalculationRules);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveExamples);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeExamples);

        // When
        String result = promptBuilderService.buildPromptOptimized(sampleSupportContent);

        // Then
        // 예시 포맷 검증: "- 예: "월 50만원 지원", "교통비 지급""
        assertTrue(result.contains("- 예: \"월 50만원 지원\", \"교통비 지급\""));
        assertTrue(result.contains("- 예: \"창업교육 제공\""));
    }

    @Test
    @DisplayName("JSON 응답 형식 검증")
    void verifyJsonResponseFormat() {
        // Given
        when(promptConditionMapper.findConditionsByType(any()))
                .thenReturn(Collections.emptyList());
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(Collections.emptyList());
        when(promptConditionMapper.findExamplesByType(any()))
                .thenReturn(Collections.emptyList());

        // When
        String result = promptBuilderService.buildPromptOptimized(sampleSupportContent);

        // Then
        assertTrue(result.contains("{\n"));
        assertTrue(result.contains("  \"isFinancialSupport\": true,\n"));
        assertTrue(result.contains("  \"estimatedAmount\": 0,\n"));
        assertTrue(result.contains("  \"policyBenefitDescription\": \"교통비 30% 환급\"\n"));
        assertTrue(result.contains("}\n"));
    }

    // ====================================
    // 특수 문자 처리 테스트
    // ====================================

    @Test
    @DisplayName("특수 문자가 포함된 지원내용 처리")
    void handleSpecialCharacters() {
        // Given
        String specialContent = "청년에게 \"월 30만원\"의 교통비를 지원합니다. (3개월 한정)";
        when(promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveConditions);
        when(promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeConditions);
        when(promptConditionMapper.findAllCalculationRules())
                .thenReturn(mockCalculationRules);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE))
                .thenReturn(mockPositiveExamples);
        when(promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE))
                .thenReturn(mockNegativeExamples);

        // When
        String result = promptBuilderService.buildPromptOptimized(specialContent);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(specialContent));
        assertTrue(result.contains("금전적 지원이 명시된 경우"));
    }
}