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
    private String nickName;  // 닉네임 (NULL 가능, 나중에 사용 결정)
    private Integer likeCount; // 좋아요 수 (기본값 0)
    private String benefitStatus; // 혜택 상태: RECEIVED, PENDING, NOT_ELIGIBLE
    private String content;   // 리뷰 내용 (TEXT)
    private LocalDateTime createdAt; // 작성 시각
    private LocalDateTime updatedAt; // 수정 시각
}
