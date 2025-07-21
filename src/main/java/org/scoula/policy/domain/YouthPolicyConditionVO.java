package org.scoula.policy.domain;

import lombok.Data;

@Data
public class YouthPolicyConditionVO {
    private Long id;
    private Long policyId;

    private Integer minAge;
    private Integer maxAge;
    private Boolean ageLimitYn;

    private String marriageStatus;

    private Long incomeMin;
    private Long incomeMax;
    private String incomeConditionCode;
    private String incomeEtc;
    private String additionalConditions;
    private String participantTarget;
}
