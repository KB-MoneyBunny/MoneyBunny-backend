package org.scoula.policy.domain;

import lombok.Data;
import org.scoula.policy.dto.PolicyDTO;
import org.scoula.policy.util.ParseUtils;

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

    /**
     * PolicyDTO를 YouthPolicyConditionVO로 변환하는 팩토리 메서드
     */
    public static YouthPolicyConditionVO fromDTO(PolicyDTO dto, Long policyId) {
        YouthPolicyConditionVO vo = new YouthPolicyConditionVO();
        vo.setPolicyId(policyId);
        vo.setMinAge(ParseUtils.parseInteger(dto.getMinAge()));
        vo.setMaxAge(ParseUtils.parseInteger(dto.getMaxAge()));
        vo.setAgeLimitYn("Y".equalsIgnoreCase(dto.getAgeLimitYn()));
        vo.setMarriageStatus(dto.getMarriageStatus());
        vo.setIncomeMin(ParseUtils.parseLong(dto.getEarnMinAmt()));
        vo.setIncomeMax(ParseUtils.parseLong(dto.getEarnMaxAmt()));
        vo.setIncomeConditionCode(dto.getIncomeConditionCode());
        vo.setIncomeEtc(dto.getIncomeEtc());
        vo.setAdditionalConditions(dto.getAdditionalConditions());
        vo.setParticipantTarget(dto.getParticipantTarget());
        return vo;
    }
}
