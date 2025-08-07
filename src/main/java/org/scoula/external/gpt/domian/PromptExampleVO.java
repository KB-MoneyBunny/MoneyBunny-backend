package org.scoula.external.gpt.domian;

import lombok.Data;

@Data
public class PromptExampleVO {

    private Long id;

    // 입력 텍스트 (지원내용)
    private String supportContent;

    // 정답 라벨 (금전적 지원 여부)
    private Boolean isFinancialSupport;

    // 추정 지원 금액
    private Integer estimatedAmount;

    // 간단한 요약 문장
    private String policyBenefitDescription;

    // 생성일 (선택 사항)
    private String createdAt; // 또는 LocalDateTime
}
