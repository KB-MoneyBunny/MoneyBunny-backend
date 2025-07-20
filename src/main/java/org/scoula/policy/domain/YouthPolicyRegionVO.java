package org.scoula.policy.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class YouthPolicyRegionVO {

    private Long id;
    private Long policyId;
    private Long regionId;
    private LocalDateTime createdAt;
}
