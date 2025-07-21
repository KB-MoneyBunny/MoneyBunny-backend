package org.scoula.policy.domain.keyword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyKeywordVO {
    private Long policyId;
    private Long keywordId;
    private LocalDateTime createdAt;
}

