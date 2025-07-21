package org.scoula.policy.domain.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyEmploymentStatusVO {
    private Long policyId;
    private Long employmentStatusId;
    private LocalDateTime createdAt;
}

