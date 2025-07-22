package org.scoula.external.gpt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GptResponseDto {

    @JsonProperty("isFinancialSupport")
    private boolean isFinancialSupport;

    @JsonProperty("estimatedAmount")
    private long estimatedAmount;

    private String policyBenefitDescription;
}
