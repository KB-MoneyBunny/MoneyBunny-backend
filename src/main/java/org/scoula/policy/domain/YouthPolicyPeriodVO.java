package org.scoula.policy.domain;

import lombok.Data;
import java.time.LocalDate;

@Data
public class YouthPolicyPeriodVO {
    private Long id;
    private Long policyId;

    private String applyPeriod;
    private LocalDate bizStartDate;
    private LocalDate bizEndDate;
    private String bizPeriodEtc;
}
