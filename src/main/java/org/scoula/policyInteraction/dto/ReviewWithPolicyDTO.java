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
public class ReviewWithPolicyDTO {
    
    // 리뷰 정보
    private Long reviewId;
    private Short rating;
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 정책 정보
    private Long policyId;
    private String policyTitle;
    private String policyDescription;
    private String policyBenefitDescription;
    private String applyPeriod;
    private Long policyBenefitAmount;
}