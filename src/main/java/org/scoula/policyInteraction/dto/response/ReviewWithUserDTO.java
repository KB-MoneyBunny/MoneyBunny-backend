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
    private String nickName;      // 리뷰 작성 시 닉네임 (NULL 가능)
    private Integer likeCount;    // 좋아요 수
    private String benefitStatus; // 혜택 상태: RECEIVED, PENDING, NOT_ELIGIBLE
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 사용자 정보 (users 테이블에서 조회)
    private Long userId;
    private String userName;      // users 테이블의 name
    private String userNickname;  // users 테이블의 nickname (있을 경우)
}