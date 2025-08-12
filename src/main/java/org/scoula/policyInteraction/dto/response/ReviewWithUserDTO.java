package org.scoula.policyInteraction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 툭종 정책 -> 여러 사용자 리뷰 (사용자 정보 포함)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewWithUserDTO {
    
    // 리뷰 정보
    private Long reviewId;
    private Integer likeCount;    // 좋아요 수 (Redis 실시간 데이터로 업데이트)
    private String benefitStatus; // 혜택 상태: RECEIVED, PENDING, NOT_ELIGIBLE
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 사용자 정보 (users 테이블에서 조회)
    private Long userId;
    private String userName;      // users 테이블의 name (마스킹 처리됨)
    private Integer profileImageId;  // users 테이블의 profile_image_id
}