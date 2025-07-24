package org.scoula.policy.domain;

import lombok.Data;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class YouthPolicyVO {

    private Long id;
    private String policyNo;
    private String title;
    private String description;
    private String supportContent;
    private String applicationMethod;
    private String screeningMethod;
    private String submitDocuments;
    private Long policyBenefitAmount;
    private String etcNotes;
    private String applyUrl;
    private String refUrl1;
    private String refUrl2;
    private Boolean isFinancialSupport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;

    private String policyBenefitDescription;

    // 정책이 포함하는 키워드 리스트
    private List<PolicyKeywordVO> keywordList;

    // 정책이 적용되는 지역 리스트
    private List<PolicyRegionVO> regionList;

    // 추가할 네 가지 정책 조건 필드
    private List<PolicyMajorVO> majorList;
    private List<PolicyEducationLevelVO> educationLevelList;
    private List<PolicyEmploymentStatusVO> employmentStatusList;
    private List<PolicySpecialConditionVO> specialConditionList;
}

