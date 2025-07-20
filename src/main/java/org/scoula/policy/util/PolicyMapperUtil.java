package org.scoula.policy.util;

import org.scoula.policy.domain.*;
import org.scoula.policy.dto.PolicyDTO;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PolicyMapperUtil {

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
        vo.setCreatedAt(parseDateTime(dto.getCreatedDate())); //
        vo.setUpdatedAt(parseDateTime(dto.getModifiedDate())); //

        // 현재 시간 기준으로 등록/수정일 기록  -> update 로직은 일단 안 넣었음 추후에 만들 수도 ?
        LocalDateTime now = LocalDateTime.now();
        vo.setCreatedAt(now);
        vo.setUpdatedAt(now);
        return vo;
    }

    // 정책 조건 VO 변환
    public static YouthPolicyConditionVO toConditionVO(PolicyDTO dto, Long policyId) {
        YouthPolicyConditionVO vo = new YouthPolicyConditionVO();
        vo.setPolicyId(policyId);
        vo.setMinAge(parseInteger(dto.getMinAge()));
        vo.setMaxAge(parseInteger(dto.getMaxAge()));
        vo.setAgeLimitYn("Y".equalsIgnoreCase(dto.getAgeLimitYn()));
//        vo.setRegionCode(dto.getRegionCode());
        vo.setMarriageStatus(dto.getMarriageStatus());

        vo.setEmploymentStatus(dto.getEmployment_status());     // jobCd
        vo.setEducationLevel(dto.getEducation_level());         // schoolCd
        vo.setMajor(dto.getMajor());                            // plcyMajorCd
        vo.setSpecialCondition(dto.getSpecialCondition());      // splzRlmRqisCn

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
        if (raw != null && !raw.trim().isEmpty()) {
            for (String word : raw.split(",")) {
                String keyword = word.trim();
                if (!keyword.isEmpty()) {
                    PolicyKeywordVO vo = new PolicyKeywordVO();
                    vo.setKeyword(keyword);
                    list.add(vo);
                }
            }
        }
        return list;
    }
    // 지역 코드 분리
    public static List<PolicyRegionVO> toRegionList(String raw) {
        List<PolicyRegionVO> list = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String code : raw.split(",")) {
                String regionCode = code.trim();
                if (!regionCode.isEmpty()) {
                    PolicyRegionVO vo = new PolicyRegionVO();
                    vo.setRegionCode(regionCode);
                    list.add(vo);
                }
            }
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

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        try {
            if (dateTimeStr.matches("\\d{14}")) { // 예: 20230719123500
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
            return LocalDateTime.parse(dateTimeStr); // ISO 형식 허용
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
