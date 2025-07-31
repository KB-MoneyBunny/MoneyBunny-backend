package org.scoula.policyInteraction.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.userPolicy.domain.UserVectorVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.BookmarkWithPolicyDTO;

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
}
