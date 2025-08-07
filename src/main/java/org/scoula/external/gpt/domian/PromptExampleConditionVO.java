package org.scoula.external.gpt.domian;

import lombok.Data;

@Data
public class PromptExampleConditionVO {

    private Long id;

    // FK - 조건 ID
    private Long conditionId;

    // FK - 예시 ID
    private Long exampleId;

    private String createdAt;
}