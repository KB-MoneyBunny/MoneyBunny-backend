package org.scoula.policy.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class YouthPolicyKeywordVO {
    private Long id;
    private Long policyId;
    private Long keywordId;
    private LocalDateTime createdAt;
}
