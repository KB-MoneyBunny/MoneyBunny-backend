package org.scoula.policy.domain;

import lombok.Data;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;
import org.scoula.policy.dto.PolicyDTO;

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
    private String largeCategory;
    private String mediumCategory;

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

    private Long view;

    /**
     * PolicyDTO를 YouthPolicyVO로 변환하는 팩토리 메서드
     */
    public static YouthPolicyVO fromDTO(PolicyDTO dto) {
        YouthPolicyVO vo = new YouthPolicyVO();
        vo.setPolicyNo(dto.getPolicyNo());
        vo.setTitle(dto.getTitle());
        vo.setDescription(dto.getDescription());
        vo.setSupportContent(dto.getSupportContent());
        vo.setApplicationMethod(dto.getApplicationMethod());
        vo.setScreeningMethod(dto.getScreeningMethod());
        vo.setSubmitDocuments(dto.getSubmitDocuments());
        vo.setPolicyBenefitAmount(null); // GPT 후처리 예정
        vo.setEtcNotes(dto.getEtcNotes());
        vo.setApplyUrl(dto.getApplyUrl());
        vo.setRefUrl1(dto.getRefUrl1());
        vo.setRefUrl2(dto.getRefUrl2());
        vo.setIsFinancialSupport(null); // GPT 후처리 예정
        vo.setPolicyBenefitDescription(null);
        vo.setViews(dto.getViews());
        vo.setLargeCategory(dto.getLargeCategory());
        vo.setMediumCategory(dto.getMediumCategory());
        return vo;
    }
}

