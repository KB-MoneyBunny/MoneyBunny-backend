package org.scoula.policyInteraction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationWithPolicyDTO {
    
    // 신청 정보
    private Long applicationId;
    private Boolean isApplied;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appliedAt;
    
    // 정책 기본 정보
    private Long policyId;
    private String title;
    private String description;
    private String policyBenefitDescription;
    
    // 신청 기간 정보
    private String applyPeriod;
    
    // 금액 정보
    private Long policyBenefitAmount;
}