package org.scoula.external.gpt.domian;

import lombok.Data;

@Data
public class PromptConditionVO {

    private Long id;

    // 조건 설명 텍스트
    private String conditionText;

    // POSITIVE / NEGATIVE 구분
    private String type;

    // 조건 사용 여부 (A/B 테스트 등에서 활용 가능)
    private Boolean isActive;

    // 생성일시 (선택)
    private String createdAt;
}