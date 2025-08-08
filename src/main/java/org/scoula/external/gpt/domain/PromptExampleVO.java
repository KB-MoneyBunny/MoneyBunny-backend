package org.scoula.external.gpt.domain;

import lombok.Data;
import org.scoula.external.gpt.domain.PromptConditionType;

@Data
public class PromptExampleVO {

    private Long id;

    // 예시 텍스트
    private String exampleText;

    // POSITIVE / NEGATIVE 구분 (enum 사용)
    private PromptConditionType type;

    // 생성일시
    private String createdAt;
}
