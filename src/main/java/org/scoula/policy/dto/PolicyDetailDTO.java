package org.scoula.policy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.scoula.policy.domain.YouthPolicyVO;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PolicyDetailDTO extends YouthPolicyVO {
    // YouthPolicyConditionVO Fields
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

    // YouthPolicyPeriodVO Fields
    private String applyPeriod;
    private LocalDate bizStartDate;
    private LocalDate bizEndDate;
    private String bizPeriodEtc;

    // Related Data
    private List<PolicyRegionVO> regions;
    private List<PolicyEducationLevelVO> educationLevels;
    private List<PolicyMajorVO> majors;
    private List<PolicyEmploymentStatusVO> employmentStatuses;
    private List<PolicySpecialConditionVO> specialConditions;
}
