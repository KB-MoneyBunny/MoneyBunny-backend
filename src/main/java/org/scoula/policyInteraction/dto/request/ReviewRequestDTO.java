package org.scoula.policyInteraction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDTO {
    
    private String benefitStatus; // 혜택 상태: RECEIVED, PENDING, NOT_ELIGIBLE
    private String content;
}