package org.scoula.external.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GptRequestDto {
    private String supportContent;

    public String toPrompt() {
        return String.format("""
        다음은 청년 정책의 '지원내용'이다.

        지원내용: %s

        이 정책이 다음 조건에 해당하는 경우 isFinancialSupport를 true로 판단한다:

        - 노동 없이 개인에게 지급되는 현금 또는 현금성 지원
        - 정기 지원, 일시금, 정률 캐시백 등 자산 형성 또는 지출 절감 효과가 명확한 경우
        - 예: 교통비 30%% 환급, 통신비 월 1만원 감면, 월 20만원 지급 등

        다음 조건에 해당하는 경우 false로 판단한다:

        - 근로 대가로 발생하는 시급/월급 등
        - 실비 보전(식비, 숙박비, 교통비 등)
        - 대출, 이자, 보증, 보험 등 간접 지원
        - 교육, 공간, 장비, 컨설팅 등 현물 또는 서비스 제공
        - 기관 예산, 정책 총 사업비 등 개인 수혜가 아님

        추정 가능한 경우 estimatedAmount를 원 단위 정수로 계산한다:

        - 월별 정기 지원은 지급 기간을 곱해 총액으로 계산
        - 일시금은 지급 기준이 명확하면 그대로 반환
        - 정률 환급은 지출 금액이 명시된 경우에만 계산 (없으면 0 반환)

        지원 형태를 policyBenefitDescription에 간단히 서술한다:

        - 예: "월 30만원 정기 지원", "교통비 30%% 환급", "금전적 지원 없음"

        결과는 다음 JSON 형식으로 정확히 반환한다. 설명은 포함하지 않는다.

        {
          "isFinancialSupport": true,
          "estimatedAmount": 0,
          "policyBenefitDescription": "교통비 30%% 환급"
        }
        """, supportContent);
    }
}
