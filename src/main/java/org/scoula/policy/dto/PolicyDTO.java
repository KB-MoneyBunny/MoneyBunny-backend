package org.scoula.policy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PolicyDTO {

    // === 정책 메인 정보 (youth_policy) ===
    @JsonProperty("plcyNo")
    private String policyNo;

    @JsonProperty("plcyNm")
    private String title;

    @JsonProperty("plcyExplnCn")
    private String description;

    @JsonProperty("plcySprtCn")
    private String supportContent;

    @JsonProperty("plcyKywdNm")
    private String keywordRaw;

    @JsonProperty("plcyAplyMthdCn")
    private String applicationMethod;

    @JsonProperty("srngMthdCn")
    private String screeningMethod;

    @JsonProperty("sbmsnDcmntCn")
    private String submitDocuments;

    @JsonProperty("etcMttrCn")
    private String etcNotes;

    @JsonProperty("aplyUrlAddr")
    private String applyUrl;

    @JsonProperty("refUrlAddr1")
    private String refUrl1;

    @JsonProperty("refUrlAddr2")
    private String refUrl2;

    @JsonProperty("frstRegDt")
    private String createdDate;

    @JsonProperty("lastMdfcnDt")
    private String modifiedDate;

    // === 조건 정보 (youth_policy_condition) ===
    @JsonProperty("sprtTrgtMinAge")
    private String minAge;

    @JsonProperty("sprtTrgtMaxAge")
    private String maxAge;

    @JsonProperty("sprtTrgtAgeLmtYn")
    private String ageLimitYn;

    @JsonProperty("zipCd")
    private String regionCode;

    @JsonProperty("mrgSttsCd")
    private String marriageStatus;

    @JsonProperty("schoolCd")
    private String educationLevel;

    @JsonProperty("plcyMajorCd")
    private String major;

    @JsonProperty("jobCd")
    private String employmentStatus;

    @JsonProperty("splzRlmRqisCn")
    private String specialCondition;

    @JsonProperty("earnMinAmt")
    private String earnMinAmt;

    @JsonProperty("earnMaxAmt")
    private String earnMaxAmt;

    @JsonProperty("earnCndSeCd")
    private String incomeConditionCode;

    @JsonProperty("earnEtcCn")
    private String incomeEtc;

    @JsonProperty("addAplyQlfcCndCn")
    private String additionalConditions;

    @JsonProperty("ptcpPrpTrgtCn")
    private String participantTarget;

    // === 기간 정보 (youth_policy_period) ===
    @JsonProperty("aplyYmd")
    private String applyPeriod;

    @JsonProperty("bizPrdBgngYmd")
    private String bizStartDate;

    @JsonProperty("bizPrdEndYmd")
    private String bizEndDate;

    @JsonProperty("bizPrdEtcCn")
    private String bizPeriodEtc;
}
