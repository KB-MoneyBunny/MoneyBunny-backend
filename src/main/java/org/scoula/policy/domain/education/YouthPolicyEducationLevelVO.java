package org.scoula.policy.domain.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyEducationLevelVO {
    private Long policyId;
    private Long educationLevelId;
    private LocalDateTime createdAt;
}

