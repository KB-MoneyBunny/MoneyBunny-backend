package org.scoula.policyInteraction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.common.util.RedisUtil;
import org.scoula.policy.domain.PolicyVectorVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.UserPolicyReviewVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.response.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.response.BookmarkWithPolicyDTO;
import org.scoula.policyInteraction.exception.ReviewException;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.scoula.policyInteraction.util.ProfanityFilter;
import org.scoula.userPolicy.domain.UserVectorVO;
import org.scoula.userPolicy.mapper.UserPolicyMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyInteractionService 단위 테스트")
class PolicyInteractionServiceTest {

    @Mock
    private PolicyInteractionMapper policyInteractionMapper;
    
    @Mock
    private PolicyMapper policyMapper;
    
    @Mock
    private UserPolicyMapper userPolicyMapper;
    
    @Mock
    private RedisUtil redisUtil;
    
    @Mock
    private ProfanityFilter profanityFilter;

    @InjectMocks
    private PolicyInteractionService policyInteractionService;
    
    private Long userId;
    private Long policyId;
    private YouthPolicyBookmarkVO bookmark;
    private UserPolicyApplicationVO application;
    private UserPolicyReviewVO review;

    @BeforeEach
    void setUp() {
        // @Autowired 필드에 Mock 주입
        org.springframework.test.util.ReflectionTestUtils.setField(
            policyInteractionService, "profanityFilter", profanityFilter);
        
        userId = 1L;
        policyId = 100L;
        
        bookmark = YouthPolicyBookmarkVO.builder()
                .userId(userId)
                .policyId(policyId)
                .createdAt(LocalDateTime.now())
                .build();
                
        application = UserPolicyApplicationVO.builder()
                .userId(userId)
                .policyId(policyId)
                .isApplied(false)
                .benefitStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
                
        review = UserPolicyReviewVO.builder()
                .userId(userId)
                .policyId(policyId)
                .benefitStatus("RECEIVED")
                .content("좋은 정책입니다.")
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ====================================
    // 북마크 관련 테스트
    // ====================================

    @Test
    @DisplayName("북마크 추가 - 성공")
    void addBookmark_Success() {
        // Given
        when(policyInteractionMapper.selectBookmark(userId, policyId)).thenReturn(null);
        when(policyInteractionMapper.insertBookmark(any(YouthPolicyBookmarkVO.class))).thenReturn(1);

        // When
        boolean result = policyInteractionService.addBookmark(userId, policyId);

        // Then
        assertTrue(result);
        verify(policyInteractionMapper).selectBookmark(userId, policyId);
        verify(policyInteractionMapper).insertBookmark(any(YouthPolicyBookmarkVO.class));
    }

    @Test
    @DisplayName("북마크 추가 - 중복 북마크로 실패")
    void addBookmark_Duplicate() {
        // Given
        when(policyInteractionMapper.selectBookmark(userId, policyId)).thenReturn(bookmark);

        // When
        boolean result = policyInteractionService.addBookmark(userId, policyId);

        // Then
        assertFalse(result);
        verify(policyInteractionMapper).selectBookmark(userId, policyId);
        verify(policyInteractionMapper, never()).insertBookmark(any());
    }

    @Test
    @DisplayName("북마크 삭제 - 성공")
    void removeBookmark_Success() {
        // Given
        when(policyInteractionMapper.deleteBookmark(userId, policyId)).thenReturn(1);

        // When
        boolean result = policyInteractionService.removeBookmark(userId, policyId);

        // Then
        assertTrue(result);
        verify(policyInteractionMapper).deleteBookmark(userId, policyId);
    }

    @Test
    @DisplayName("사용자 북마크 목록 조회")
    void getUserBookmarks() {
        // Given
        BookmarkWithPolicyDTO bookmark1 = BookmarkWithPolicyDTO.builder()
                .policyId(100L)
                .title("청년 취업 지원")
                .build();
        BookmarkWithPolicyDTO bookmark2 = BookmarkWithPolicyDTO.builder()
                .policyId(101L)
                .title("청년 주거 지원")
                .build();
        List<BookmarkWithPolicyDTO> bookmarks = Arrays.asList(bookmark1, bookmark2);
        
        when(policyInteractionMapper.selectBookmarksByUserId(userId)).thenReturn(bookmarks);

        // When
        List<BookmarkWithPolicyDTO> result = policyInteractionService.getUserBookmarks(userId);

        // Then
        assertEquals(2, result.size());
        assertEquals("청년 취업 지원", result.get(0).getTitle());
        verify(policyInteractionMapper).selectBookmarksByUserId(userId);
    }

    // ====================================
    // 신청 관련 테스트
    // ====================================

    @Test
    @DisplayName("정책 신청 등록 - 성공")
    void addApplication_Success() {
        // Given
        when(policyInteractionMapper.selectApplication(userId, policyId)).thenReturn(null);
        when(policyInteractionMapper.insertApplication(any(UserPolicyApplicationVO.class))).thenReturn(1);

        // When
        boolean result = policyInteractionService.addApplication(userId, policyId);

        // Then
        assertTrue(result);
        verify(policyInteractionMapper).selectApplication(userId, policyId);
        verify(policyInteractionMapper).insertApplication(any(UserPolicyApplicationVO.class));
    }

    @Test
    @DisplayName("정책 신청 완료 처리 - 성공")
    void completeApplication_Success() {
        // Given
        when(policyInteractionMapper.selectApplication(userId, policyId)).thenReturn(application);
        when(policyInteractionMapper.updateApplicationToComplete(userId, policyId)).thenReturn(1);

        // When
        boolean result = policyInteractionService.completeApplication(userId, policyId);

        // Then
        assertTrue(result);
        verify(policyInteractionMapper).selectApplication(userId, policyId);
        verify(policyInteractionMapper).updateApplicationToComplete(userId, policyId);
    }

    @Test
    @DisplayName("정책 신청 완료 처리 - 신청 기록 없음으로 실패")
    void completeApplication_NoApplication() {
        // Given
        when(policyInteractionMapper.selectApplication(userId, policyId)).thenReturn(null);

        // When
        boolean result = policyInteractionService.completeApplication(userId, policyId);

        // Then
        assertFalse(result);
        verify(policyInteractionMapper).selectApplication(userId, policyId);
        verify(policyInteractionMapper, never()).updateApplicationToComplete(any(), any());
    }

    @Test
    @DisplayName("미완료 신청 정책 조회")
    void getIncompleteApplication() {
        // Given
        ApplicationWithPolicyDTO incompleteApp = ApplicationWithPolicyDTO.builder()
                .policyId(policyId)
                .title("청년 창업 지원")
                .isApplied(false)
                .build();
        when(policyInteractionMapper.findIncompleteApplication(userId)).thenReturn(incompleteApp);

        // When
        ApplicationWithPolicyDTO result = policyInteractionService.getIncompleteApplication(userId);

        // Then
        assertNotNull(result);
        assertEquals(policyId, result.getPolicyId());
        assertFalse(result.getIsApplied());
        verify(policyInteractionMapper).findIncompleteApplication(userId);
    }

    // ====================================
    // 리뷰 관련 테스트
    // ====================================

    @Test
    @DisplayName("리뷰 작성 - 성공")
    void addReview_Success() {
        // Given
        String content = "정말 좋은 정책입니다.";
        String benefitStatus = "RECEIVED";
        
        UserPolicyApplicationVO completedApplication = UserPolicyApplicationVO.builder()
                .userId(userId)
                .policyId(policyId)
                .isApplied(true)
                .benefitStatus("RECEIVED")
                .build();
        
        when(profanityFilter.containsProfanity(content)).thenReturn(false);
        when(policyInteractionMapper.selectApplication(userId, policyId)).thenReturn(completedApplication);
        when(policyInteractionMapper.selectReviewByUserAndPolicy(userId, policyId, benefitStatus)).thenReturn(null);
        when(policyInteractionMapper.insertReview(any(UserPolicyReviewVO.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            policyInteractionService.addReview(userId, policyId, benefitStatus, content)
        );
        
        verify(profanityFilter).containsProfanity(content);
        verify(policyInteractionMapper).insertReview(any(UserPolicyReviewVO.class));
    }

    @Test
    @DisplayName("리뷰 작성 - 욕설 포함으로 실패")
    void addReview_ProfanityDetected() {
        // Given
        String content = "욕설이 포함된 내용";
        String benefitStatus = "RECEIVED";
        
        when(profanityFilter.containsProfanity(content)).thenReturn(true);

        // When & Then
        ReviewException exception = assertThrows(ReviewException.class, () ->
            policyInteractionService.addReview(userId, policyId, benefitStatus, content)
        );
        
        verify(profanityFilter).containsProfanity(content);
        verify(policyInteractionMapper, never()).insertReview(any());
    }

    @Test
    @DisplayName("리뷰 작성 - 신청하지 않은 정책으로 실패")
    void addReview_NotApplied() {
        // Given
        String content = "리뷰 내용";
        String benefitStatus = "RECEIVED";
        
        when(profanityFilter.containsProfanity(content)).thenReturn(false);
        when(policyInteractionMapper.selectApplication(userId, policyId)).thenReturn(null);

        // When & Then
        ReviewException exception = assertThrows(ReviewException.class, () ->
            policyInteractionService.addReview(userId, policyId, benefitStatus, content)
        );
        
        verify(policyInteractionMapper, never()).insertReview(any());
    }

    // ====================================
    // 좋아요 관련 테스트
    // ====================================

    @Test
    @DisplayName("리뷰 좋아요 추가 - 성공")
    void addReviewLike_Success() {
        // Given
        Long reviewId = 1L;
        when(redisUtil.addLikeToReview(userId, reviewId)).thenReturn(true);
        when(policyInteractionMapper.incrementReviewLikeCount(reviewId)).thenReturn(1);

        // When
        boolean result = policyInteractionService.addReviewLike(userId, reviewId);

        // Then
        assertTrue(result);
        verify(redisUtil).addLikeToReview(userId, reviewId);
        verify(policyInteractionMapper).incrementReviewLikeCount(reviewId);
    }

    @Test
    @DisplayName("리뷰 좋아요 취소 - 성공")
    void removeReviewLike_Success() {
        // Given
        Long reviewId = 1L;
        when(redisUtil.removeLikeFromReview(userId, reviewId)).thenReturn(true);
        when(policyInteractionMapper.decrementReviewLikeCount(reviewId)).thenReturn(1);

        // When
        boolean result = policyInteractionService.removeReviewLike(userId, reviewId);

        // Then
        assertTrue(result);
        verify(redisUtil).removeLikeFromReview(userId, reviewId);
        verify(policyInteractionMapper).decrementReviewLikeCount(reviewId);
    }

    @Test
    @DisplayName("리뷰 좋아요 수 조회")
    void getReviewLikeCount() {
        // Given
        Long reviewId = 1L;
        Long expectedCount = 10L;
        when(redisUtil.getLikeCount(reviewId)).thenReturn(expectedCount);

        // When
        Long result = policyInteractionService.getReviewLikeCount(reviewId);

        // Then
        assertEquals(expectedCount, result);
        verify(redisUtil).getLikeCount(reviewId);
    }

    @Test
    @DisplayName("사용자 리뷰 좋아요 상태 확인")
    void isUserLikedReview() {
        // Given
        Long reviewId = 1L;
        when(redisUtil.isUserLikedReview(userId, reviewId)).thenReturn(true);

        // When
        boolean result = policyInteractionService.isUserLikedReview(userId, reviewId);

        // Then
        assertTrue(result);
        verify(redisUtil).isUserLikedReview(userId, reviewId);
    }
}