package org.scoula.policy.domain.specialcondition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicySpecialConditionVO {
    private Long policyId;
    private Long specialConditionId;
    private LocalDateTime createdAt;
}
