package org.scoula.policy.util;

import org.scoula.policy.domain.YouthPolicyConditionVO;
import org.scoula.policy.domain.YouthPolicyPeriodVO;
import org.scoula.policy.domain.YouthPolicyVO;
import org.scoula.policy.domain.education.*;
import org.scoula.policy.domain.employment.*;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.major.*;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.specialcondition.*;
import org.scoula.policy.dto.PolicyDTO;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PolicyMapperUtil {

    // 공통 분리 유틸
    private static List<String> splitCommaSeparated(String raw) {
        List<String> result = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String s : raw.split(",")) {
                String cleaned = s.trim();
                if (!cleaned.isEmpty()) {
                    result.add(cleaned);
                }
            }
        }
        return result;
    }

    // 정책 메인 VO 변환
    public static YouthPolicyVO toYouthPolicyVO(PolicyDTO dto) {
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

        return vo;
    }

    // 정책 조건 VO 변환
    public static YouthPolicyConditionVO toConditionVO(PolicyDTO dto, Long policyId) {
        YouthPolicyConditionVO vo = new YouthPolicyConditionVO();
        vo.setPolicyId(policyId);
        vo.setMinAge(parseInteger(dto.getMinAge()));
        vo.setMaxAge(parseInteger(dto.getMaxAge()));
        vo.setAgeLimitYn("Y".equalsIgnoreCase(dto.getAgeLimitYn()));
        vo.setMarriageStatus(dto.getMarriageStatus());

        vo.setIncomeMin(parseLong(dto.getEarnMinAmt()));
        vo.setIncomeMax(parseLong(dto.getEarnMaxAmt()));
        vo.setIncomeConditionCode(dto.getIncomeConditionCode());
        vo.setIncomeEtc(dto.getIncomeEtc());
        vo.setAdditionalConditions(dto.getAdditionalConditions());
        vo.setParticipantTarget(dto.getParticipantTarget());
        return vo;
    }

    // 정책 운영 기간 VO 변환
    public static YouthPolicyPeriodVO toPeriodVO(PolicyDTO dto, Long policyId) {
        YouthPolicyPeriodVO vo = new YouthPolicyPeriodVO();
        vo.setPolicyId(policyId);
        vo.setApplyPeriod(dto.getApplyPeriod());
        vo.setBizStartDate(parseLocalDate(dto.getBizStartDate()));
        vo.setBizEndDate(parseLocalDate(dto.getBizEndDate()));
        vo.setBizPeriodEtc(dto.getBizPeriodEtc());
        return vo;
    }

    // 키워드 분리
    public static List<PolicyKeywordVO> toKeywordList(String raw) {
        List<PolicyKeywordVO> list = new ArrayList<>();
        for (String keyword : splitCommaSeparated(raw)) {
            PolicyKeywordVO vo = new PolicyKeywordVO();
            vo.setKeyword(keyword);
            list.add(vo);
        }
        return list;
    }

    // 지역 코드 분리
    public static List<PolicyRegionVO> toRegionList(String raw) {
        List<PolicyRegionVO> list = new ArrayList<>();
        for (String regionCode : splitCommaSeparated(raw)) {
            PolicyRegionVO vo = new PolicyRegionVO();
            vo.setRegionCode(regionCode);
            list.add(vo);
        }
        return list;
    }

    // Major 분리
    public static List<PolicyMajorVO> toMajorMasterList(String raw) {
        List<PolicyMajorVO> list = new ArrayList<>();
        for (String major : splitCommaSeparated(raw)) {
            PolicyMajorVO vo = new PolicyMajorVO();
            vo.setMajor(major);
            list.add(vo);
        }
        return list;
    }

    public static List<YouthPolicyMajorVO> toMajorMappingList(List<PolicyMajorVO> majorList, Long policyId) {
        List<YouthPolicyMajorVO> list = new ArrayList<>();
        for (PolicyMajorVO major : majorList) {
            YouthPolicyMajorVO mapping = new YouthPolicyMajorVO();
            mapping.setPolicyId(policyId);
            mapping.setMajorId(major.getId()); // insert 이후 설정 필요
            mapping.setCreatedAt(LocalDateTime.now());
            list.add(mapping);
        }
        return list;
    }

    // Education Level
    public static List<PolicyEducationLevelVO> toEducationMasterList(String raw) {
        List<PolicyEducationLevelVO> list = new ArrayList<>();
        for (String edu : splitCommaSeparated(raw)) {
            PolicyEducationLevelVO vo = new PolicyEducationLevelVO();
            vo.setEducationLevel(edu);
            list.add(vo);
        }
        return list;
    }

    public static List<YouthPolicyEducationLevelVO> toEducationMappingList(List<PolicyEducationLevelVO> eduList, Long policyId) {
        List<YouthPolicyEducationLevelVO> list = new ArrayList<>();
        for (PolicyEducationLevelVO edu : eduList) {
            YouthPolicyEducationLevelVO mapping = new YouthPolicyEducationLevelVO();
            mapping.setPolicyId(policyId);
            mapping.setEducationLevelId(edu.getId());
            mapping.setCreatedAt(LocalDateTime.now());
            list.add(mapping);
        }
        return list;
    }

    // Employment Status
    public static List<PolicyEmploymentStatusVO> toEmploymentMasterList(String raw) {
        List<PolicyEmploymentStatusVO> list = new ArrayList<>();
        for (String emp : splitCommaSeparated(raw)) {
            PolicyEmploymentStatusVO vo = new PolicyEmploymentStatusVO();
            vo.setEmploymentStatus(emp);
            list.add(vo);
        }
        return list;
    }

    public static List<YouthPolicyEmploymentStatusVO> toEmploymentMappingList(List<PolicyEmploymentStatusVO> empList, Long policyId) {
        List<YouthPolicyEmploymentStatusVO> list = new ArrayList<>();
        for (PolicyEmploymentStatusVO emp : empList) {
            YouthPolicyEmploymentStatusVO mapping = new YouthPolicyEmploymentStatusVO();
            mapping.setPolicyId(policyId);
            mapping.setEmploymentStatusId(emp.getId());
            mapping.setCreatedAt(LocalDateTime.now());
            list.add(mapping);
        }
        return list;
    }

    // Special Condition
    public static List<PolicySpecialConditionVO> toSpecialConditionMasterList(String raw) {
        List<PolicySpecialConditionVO> list = new ArrayList<>();
        for (String sc : splitCommaSeparated(raw)) {
            PolicySpecialConditionVO vo = new PolicySpecialConditionVO();
            vo.setSpecialCondition(sc);
            list.add(vo);
        }
        return list;
    }

    public static List<YouthPolicySpecialConditionVO> toSpecialConditionMappingList(List<PolicySpecialConditionVO> scList, Long policyId) {
        List<YouthPolicySpecialConditionVO> list = new ArrayList<>();
        for (PolicySpecialConditionVO sc : scList) {
            YouthPolicySpecialConditionVO mapping = new YouthPolicySpecialConditionVO();
            mapping.setPolicyId(policyId);
            mapping.setSpecialConditionId(sc.getId());
            mapping.setCreatedAt(LocalDateTime.now());
            list.add(mapping);
        }
        return list;
    }

    // 파싱 유틸
    private static Integer parseInteger(String s) {
        try {
            return s == null ? null : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long parseLong(String s) {
        try {
            return s == null ? null : Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
