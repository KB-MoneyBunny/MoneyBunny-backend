package org.scoula.userPolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor       // 기본 생성자
@AllArgsConstructor      // 모든 필드 생성자
@Builder
public class SearchResultDTO {
    private Long policyId;
    private String title;
    private String policyBenefitDescription;
    private String endDate;
    private Long policyBenefitAmount;
}
