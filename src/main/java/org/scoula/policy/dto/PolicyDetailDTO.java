package org.scoula.policy.dto;

import lombok.Data;
import org.scoula.policy.domain.YouthPolicyVO;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;

import java.time.LocalDate;
import java.util.List;

@Data
public class PolicyDetailDTO {
    // 정책 기본 정보
    private Long id;
    private String policyNo;
    private String title;
    private String description;
    private String supportContent;
    private String applicationMethod;
    private String screeningMethod;
    private String submitDocuments;
    private String policyBenefitAmount;
    private String etcNotes;
    private String applyUrl;
    private String refUrl1;
    private String refUrl2;
    private Boolean isFinancialSupport;
    private String policyBenefitDescription;
    private Integer view;

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
    private List<PolicyKeywordVO> keywords;
}
