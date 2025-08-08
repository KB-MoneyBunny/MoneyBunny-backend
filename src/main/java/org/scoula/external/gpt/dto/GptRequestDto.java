package org.scoula.external.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GPT API 요청을 위한 DTO
 * PromptBuilderService에서 생성된 동적 프롬프트를 담습니다.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class GptRequestDto {
    
    private final String prompt;
    
}