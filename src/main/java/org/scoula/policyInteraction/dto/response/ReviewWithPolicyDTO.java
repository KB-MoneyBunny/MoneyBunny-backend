package org.scoula.policyInteraction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 정책 리뷰 DTO (정책 정보 포함)
 *
 * 툭종 정책 -> 여러 사용자 리뷰 (정책 정보 포함)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewWithPolicyDTO {
    
    // 리뷰 정보
    private Long reviewId;
    private Long userId;          // 리뷰 작성자 ID
    private String userName;      // 리뷰 작성자 이름 (마스킹 처리됨)
    private Integer profileImageId; // 리뷰 작성자 프로필 이미지 ID
    private Integer likeCount;    // 좋아요 수
    private String benefitStatus; // 혜택 상태: RECEIVED, PENDING, NOT_ELIGIBLE
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 정책 정보
    private Long policyId;
    private String policyTitle;
    private String policyDescription;
    private String policyBenefitDescription;
    private String applyPeriod;
    private Long policyBenefitAmount;
}