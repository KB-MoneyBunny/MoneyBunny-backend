package org.scoula.policy.domain;

import lombok.Data;

@Data
public class YouthPolicyConditionVO {
    private Long id;
    private Long policyId;

    private Integer minAge;
    private Integer maxAge;
    private Boolean ageLimitYn;

//    private String regionCode;
    private String marriageStatus;
    private String employmentStatus;
    private String educationLevel;
    private String major;
    private String specialCondition;

    private Long incomeMin;
    private Long incomeMax;
    private String incomeConditionCode;
    private String incomeEtc;
    private String additionalConditions;
    private String participantTarget;
}
