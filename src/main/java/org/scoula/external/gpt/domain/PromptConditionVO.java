package org.scoula.external.gpt.domain;

import lombok.Data;
import org.scoula.external.gpt.domain.PromptConditionType;

@Data
public class PromptConditionVO {

    private Long id;

    // 조건 설명 텍스트
    private String conditionText;

    // POSITIVE / NEGATIVE 구분 (enum 사용)
    private PromptConditionType type;

    // 생성일시
    private String createdAt;
}