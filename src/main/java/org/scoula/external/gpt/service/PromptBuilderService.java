package org.scoula.external.gpt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.external.gpt.domain.PromptCalculationRuleVO;
import org.scoula.external.gpt.domain.PromptConditionVO;
import org.scoula.external.gpt.domain.PromptExampleVO;
import org.scoula.external.gpt.domain.PromptConditionType;
import org.scoula.external.gpt.mapper.PromptConditionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptBuilderService {

    private final PromptConditionMapper promptConditionMapper;

    /**
     * 타입별로 조건을 조회하여 프롬프트 생성 (효율적인 방식)
     */
    public String buildPromptOptimized(String supportContent) {
        List<PromptConditionVO> positiveConditions = promptConditionMapper.findConditionsByType(PromptConditionType.POSITIVE);
        List<PromptConditionVO> negativeConditions = promptConditionMapper.findConditionsByType(PromptConditionType.NEGATIVE);
        List<PromptCalculationRuleVO> calculationRules = promptConditionMapper.findAllCalculationRules();
        List<PromptExampleVO> positiveExamples = promptConditionMapper.findExamplesByType(PromptConditionType.POSITIVE);
        List<PromptExampleVO> negativeExamples = promptConditionMapper.findExamplesByType(PromptConditionType.NEGATIVE);
        
        return buildPromptString(supportContent, positiveConditions, negativeConditions,
                                calculationRules, positiveExamples, negativeExamples);
    }

    /**
     * 실제 프롬프트 문자열을 생성
     */
    private String buildPromptString(String supportContent, 
                                    List<PromptConditionVO> positiveConditions,
                                    List<PromptConditionVO> negativeConditions,
                                    List<PromptCalculationRuleVO> calculationRules,
                                    List<PromptExampleVO> positiveExamples,
                                    List<PromptExampleVO> negativeExamples) {
        
        StringBuilder prompt = new StringBuilder();
        
        // 기본 헤더
        prompt.append("다음은 청년 정책의 '지원내용'이다.\n\n");
        prompt.append("지원내용: ").append(supportContent).append("\n\n");
        
        // POSITIVE 조건들 (기존 GptRequestDto와 동일한 구조)
        prompt.append("이 정책이 다음 조건에 해당하는 경우 isFinancialSupport를 true로 판단한다:\n\n");
        for (PromptConditionVO condition : positiveConditions) {
            prompt.append("- ").append(condition.getConditionText()).append("\n");
        }
        // POSITIVE 예시들을 조건과 연결하여 추가 (기존과 동일)
        if (!positiveExamples.isEmpty()) {
            StringBuilder exampleText = new StringBuilder("- 예: ");
            for (int i = 0; i < positiveExamples.size(); i++) {
                if (i > 0) exampleText.append(", ");
                exampleText.append("\"").append(positiveExamples.get(i).getExampleText()).append("\"");
            }
            exampleText.append("\n");
            prompt.append(exampleText);
        }
        prompt.append("\n");
        
        // NEGATIVE 조건들
        prompt.append("다음 조건에 해당하는 경우 false로 판단한다:\n\n");
        for (PromptConditionVO condition : negativeConditions) {
            prompt.append("- ").append(condition.getConditionText()).append("\n");
        }
        // NEGATIVE 예시들을 조건과 연결하여 추가 (기존과 동일)
        if (!negativeExamples.isEmpty()) {
            StringBuilder exampleText = new StringBuilder("- 예: ");
            for (int i = 0; i < negativeExamples.size(); i++) {
                if (i > 0) exampleText.append(", ");
                exampleText.append("\"").append(negativeExamples.get(i).getExampleText()).append("\"");
            }
            exampleText.append("\n");
            prompt.append(exampleText);
        }
        prompt.append("\n");
        
        // 동적 계산 규칙
        prompt.append("추정 가능한 경우 estimatedAmount를 원 단위 정수로 계산한다:\n\n");
        for (PromptCalculationRuleVO rule : calculationRules) {
            prompt.append("- ").append(rule.getRuleText()).append("\n");
        }
        prompt.append("\n");
        
        prompt.append("지원 형태를 policyBenefitDescription에 간단히 서술한다:\n\n");
        prompt.append("- 예: \"월 30만원 정기 지원\", \"교통비 30% 환급\", \"금전적 지원 없음\"\n\n");
        
        // 응답 형식
        prompt.append("결과는 다음 JSON 형식으로 정확히 반환한다. 설명은 포함하지 않는다.\n\n");
        prompt.append("{\n");
        prompt.append("  \"isFinancialSupport\": true,\n");
        prompt.append("  \"estimatedAmount\": 0,\n");
        prompt.append("  \"policyBenefitDescription\": \"교통비 30% 환급\"\n");
        prompt.append("}\n");
        
        String finalPrompt = prompt.toString();
        log.debug("Generated prompt with {} positive conditions, {} negative conditions, {} calculation rules, {} positive examples, {} negative examples", 
                 positiveConditions.size(), negativeConditions.size(), calculationRules.size(),
                 positiveExamples.size(), negativeExamples.size());
        
        return finalPrompt;
    }
}
