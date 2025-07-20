package org.scoula.external.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GptRequestDto {
    private String supportContent;

    public String toPrompt() {
        return String.format("""
        아래는 청년 정책의 '지원내용'입니다.
        
        지원내용: %s

        위 내용을 바탕으로 다음 질문에 정확히 답해주세요.

        1. 이 정책이 금전적 지원을 포함하는지 판단해주세요.
           - '지원내용'에 금액(예: ○○원, ○○만원 등)이 구체적으로 명시된 경우에만 true로 판단합니다.
           - 금액이 명시되지 않거나 추정 금액이 0인 경우에는 false로 판단합니다.

        2. 금전적 지원이 명시되어 있다면, 해당 금액 중 가장 **작은 금액(최소 지원금)** 을 정수형 숫자로 원 단위로 반환해주세요.
           - 예: "월 20만원 ~ 30만원 지원" → 200000
           - 예: "최대 100만원 지원" → 1000000
           - 단위가 없거나 모호한 경우, 또는 금액이 전혀 명시되지 않았다면 0으로 반환해주세요.

        최종 결과는 아래의 JSON 형식으로 **정확히** 반환해주세요.  
        다른 설명이나 텍스트는 절대 포함하지 마세요:

        {
          "isFinancialSupport": false,
          "estimatedAmount": 0
        }
        """, supportContent);
    }
}
