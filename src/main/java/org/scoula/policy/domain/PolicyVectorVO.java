package org.scoula.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyVectorVO {
    private Long id;
    private Long policyId;
    private BigDecimal vecBenefitAmount;
    private BigDecimal vecDeadline;
    private BigDecimal vecViews;
    private LocalDateTime createdAt;
}