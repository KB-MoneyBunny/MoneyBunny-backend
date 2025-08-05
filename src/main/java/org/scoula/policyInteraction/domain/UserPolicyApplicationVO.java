package org.scoula.policyInteraction.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPolicyApplicationVO {
    private Long id;
    private Long userId;
    private Long policyId;
    private String applicationUrl;
    private Boolean isApplied;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
