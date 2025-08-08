package org.scoula.external.gpt.domain;

import lombok.Data;

@Data
public class PromptCalculationRuleVO {
    
    private Long id;
    
    // 계산 규칙 설명
    private String ruleText;
    
    
    // 생성일시
    private String createdAt;
}