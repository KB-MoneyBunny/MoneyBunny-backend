package org.scoula.policy.domain;

import lombok.Data;
import org.scoula.policy.dto.PolicyDTO;
import org.scoula.policy.util.ParseUtils;

import java.time.LocalDate;

@Data
public class YouthPolicyPeriodVO {
    private Long id;
    private Long policyId;

    private String applyPeriod;
    private LocalDate bizStartDate;
    private LocalDate bizEndDate;
    private String bizPeriodEtc;

    /**
     * PolicyDTO를 YouthPolicyPeriodVO로 변환하는 팩토리 메서드
     */
    public static YouthPolicyPeriodVO fromDTO(PolicyDTO dto, Long policyId) {
        YouthPolicyPeriodVO vo = new YouthPolicyPeriodVO();
        vo.setPolicyId(policyId);
        vo.setApplyPeriod(dto.getApplyPeriod());
        vo.setBizStartDate(ParseUtils.parseLocalDate(dto.getBizStartDate()));
        vo.setBizEndDate(ParseUtils.parseLocalDate(dto.getBizEndDate()));
        vo.setBizPeriodEtc(dto.getBizPeriodEtc());
        return vo;
    }
}
