package org.scoula.policyInteraction.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class UserPolicyReviewVO {

    private Long id;          // 리뷰 고유 ID (PK)
    private Long policyId;    // FK -> youth_policy.id
    private Long userId;      // FK -> users.user_id
    private Short rating;     // 1~5 별점 (TINYINT 매핑 시 short 권장)
    private String content;   // 리뷰 내용 (TEXT)
    private LocalDateTime createdAt; // 작성 시각
    private LocalDateTime updatedAt; // 수정 시각
}
