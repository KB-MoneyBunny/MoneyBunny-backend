package org.scoula.policyInteraction.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.UserPolicyReviewVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.response.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.response.BookmarkWithPolicyDTO;
import org.scoula.policyInteraction.dto.response.ReviewWithUserDTO;
import org.scoula.policyInteraction.dto.response.ReviewWithPolicyDTO;

import java.util.List;

/**
 * 사용자의 정책 상호작용(북마크 및 신청)을 처리하는 Mapper 인터페이스
 */
@Mapper
public interface PolicyInteractionMapper {

    // ────────────────────────────────────────
    // 📌 정책 북마크 관련
    // ────────────────────────────────────────

    /** 북마크 등록 */
    int insertBookmark(YouthPolicyBookmarkVO bookmark);

    /** 북마크 삭제 */
    int deleteBookmark(@Param("userId") Long userId, @Param("policyId") Long policyId);

    /** 특정 정책에 대한 사용자의 북마크 단건 조회 */
    YouthPolicyBookmarkVO selectBookmark(@Param("userId") Long userId, @Param("policyId") Long policyId);

    /** 사용자의 전체 북마크 목록 조회 (정책 정보 포함) */
    List<BookmarkWithPolicyDTO> selectBookmarksByUserId(@Param("userId") Long userId);

    /** 모든 북마크 목록 조회 (알림 발송용) */
    List<YouthPolicyBookmarkVO> getAllBookmarks();

    /** 북마크 알림을 구독한 사용자의 북마크만 조회 (최적화된 알림 발송용) */
    List<YouthPolicyBookmarkVO> getBookmarksWithActiveSubscription();

    // ────────────────────────────────────────
    // 📌 정책 신청 관련
    // ────────────────────────────────────────

    /** 정책 신청 등록 */
    int insertApplication(UserPolicyApplicationVO application);

    /**
     * 특정 정책에 대한 사용자의 신청 단건 조회
     */
    UserPolicyApplicationVO selectApplication(@Param("userId") Long userId, @Param("policyId") Long policyId);

    /** 사용자의 전체 신청 목록 조회 (정책 정보 포함) */
    List<ApplicationWithPolicyDTO> selectApplicationsByUserId(@Param("userId") Long userId);

    /** 정책 신청 완료 처리 (is_applied를 true로 변경) */
    int updateApplicationToComplete(@Param("userId") Long userId, @Param("policyId") Long policyId);

    /** 정책 신청 기록 삭제 */
    int deleteApplication(@Param("userId") Long userId, @Param("policyId") Long policyId);

    /** 미완료 신청 정책 하나 조회 (is_applied = false) */
    ApplicationWithPolicyDTO findIncompleteApplication(@Param("userId") Long userId);

    /** 혜택 수령 상태 업데이트 */
    int updateBenefitStatus(@Param("userId") Long userId, @Param("policyId") Long policyId, @Param("benefitStatus") String benefitStatus);

    // ────────────────────────────────────────
    // 📌 정책 리뷰 관련
    // ────────────────────────────────────────

    /** 리뷰 등록 */
    int insertReview(UserPolicyReviewVO review);

    /** 리뷰 수정 */
    int updateReview(UserPolicyReviewVO review);

    /** 리뷰 삭제 */
    int deleteReview(@Param("userId") Long userId, @Param("policyId") Long policyId, @Param("benefitStatus") String benefitStatus);

    /** 사용자의 특정 정책 리뷰 조회 */
    UserPolicyReviewVO selectReviewByUserAndPolicy(@Param("userId") Long userId, @Param("policyId") Long policyId, @Param("benefitStatus") String benefitStatus);

    /** 정책별 모든 리뷰 조회 (사용자 정보 포함) */
    List<ReviewWithUserDTO> selectReviewsByPolicyId(@Param("policyId") Long policyId);

    /** 정책별 리뷰 수 조회 */
    Integer selectReviewCountByPolicyId(@Param("policyId") Long policyId);

    /** 사용자가 작성한 모든 리뷰 조회 */
    List<ReviewWithPolicyDTO> selectReviewsByUserId(@Param("userId") Long userId);
    
    /** 전체 리뷰 조회 (관리자 전용) */
    List<ReviewWithUserDTO> selectAllReviewsWithUser();
    
    /** 정책별 모든 리뷰 삭제 (관리자 전용) */
    int deleteReviewsByPolicyId(@Param("policyId") Long policyId);
    
    // ────────────────────────────────────────
    // 📌 좋아요 시스템 관련 (하이브리드: Redis + DB 동기화)
    // ────────────────────────────────────────
    
    /** 리뷰 좋아요 수 증가 (DB 동기화용) */
    int incrementReviewLikeCount(@Param("reviewId") Long reviewId);
    
    /** 리뷰 좋아요 수 감소 (DB 동기화용) */
    int decrementReviewLikeCount(@Param("reviewId") Long reviewId);
}
