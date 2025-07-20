package org.scoula.policy.domain.region;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyRegionVO {
    private Long policyId;
    private Long regionId;
    private LocalDateTime createdAt;
}
