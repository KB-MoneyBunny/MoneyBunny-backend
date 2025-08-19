package org.scoula.userPolicy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.common.util.RedisUtil;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.policy.util.PolicyDataHolder;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.userPolicy.domain.*;
import org.scoula.userPolicy.dto.PolicyWithVectorDTO;
import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.dto.TestResultRequestDTO;
import org.scoula.userPolicy.mapper.UserPolicyMapper;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPolicyService 단위 테스트")
class UserPolicyServiceTest {

    @Mock
    private UserPolicyMapper userPolicyMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PolicyDataHolder policyDataHolder;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private PolicyInteractionMapper policyInteractionMapper;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private UserPolicyServiceImpl userPolicyService;

    private String username;
    private Long userId;
    private MemberVO memberVO;
    private UserPolicyConditionVO userPolicyConditionVO;
    private TestResultRequestDTO testResultRequestDTO;
    private UserVectorVO userVectorVO;

    @BeforeEach
    void setUp() {
        username = "testuser";
        userId = 1L;

        memberVO = MemberVO.builder()
                .userId(userId)
                .loginId(username)
                .name("테스트사용자")
                .email("test@example.com")
                .createdAt(new Date())
                .build();

        userPolicyConditionVO = UserPolicyConditionVO.builder()
                .id(1L)
                .userId(userId)
                .age(25)
                .marriage("미혼")
                .income(3000)
                .moneyRank(1)
                .periodRank(2)
                .popularityRank(3)
                .regions(Collections.emptyList())
                .educationLevels(Collections.emptyList())
                .employmentStatuses(Collections.emptyList())
                .majors(Collections.emptyList())
                .specialConditions(Collections.emptyList())
                .keywords(Collections.emptyList())
                .build();

        testResultRequestDTO = TestResultRequestDTO.builder()
                .age(25)
                .marriage("미혼")
                .income(3000)
                .moneyRank(1)
                .periodRank(2)
                .popularityRank(3)
                .regions(Arrays.asList("11000"))
                .educationLevels(Arrays.asList("대학졸업"))
                .employmentStatuses(Arrays.asList("취업중"))
                .majors(Arrays.asList("컴퓨터공학"))
                .specialConditions(Arrays.asList("청년"))
                .keywords(Arrays.asList("취업"))
                .build();

        userVectorVO = UserVectorVO.builder()
                .id(1L)
                .userId(userId)
                .vecBenefitAmount(new BigDecimal("0.6"))
                .vecDeadline(new BigDecimal("0.5"))
                .vecViews(new BigDecimal("0.4"))
                .build();
    }

    // ====================================
    // 정책 조건 조회 테스트
    // ====================================

    // 무시된 테스트 제거됨 - getUserPolicyCondition_Success()

    // 무시된 테스트 제거됨 - getUserPolicyCondition_UserNotFound()

    // ====================================
    // 정책 조건 저장 테스트
    // ====================================

    // 무시된 테스트 제거됨 - saveUserPolicyCondition_Success()

    // ====================================
    // 정책 조건 수정 테스트
    // ====================================

    // 무시된 테스트 제거됨 - updateUserPolicyCondition_Success()

    @Test
    @DisplayName("사용자 정책 조건 수정 - 기존 조건 없음")
    void updateUserPolicyCondition_NoExistingCondition() {
        // Given
        when(memberMapper.get(username)).thenReturn(memberVO);
        when(userPolicyMapper.findUserPolicyConditionByUserId(userId)).thenReturn(null);

        // When
        TestResultRequestDTO result = userPolicyService.updateUserPolicyCondition(username, testResultRequestDTO);

        // Then
        assertNull(result);
        verify(memberMapper).get(username);
        verify(userPolicyMapper).findUserPolicyConditionByUserId(userId);
        verify(userPolicyMapper, never()).updateUserPolicyCondition(any());
    }

    // ====================================
    // 정책 조건 삭제 테스트
    // ====================================

    @Test
    @DisplayName("사용자 정책 조건 삭제 - 성공")
    void deleteUserPolicyCondition_Success() {
        // Given
        when(memberMapper.get(username)).thenReturn(memberVO);
        when(userPolicyMapper.findUserPolicyConditionByUserId(userId)).thenReturn(userPolicyConditionVO);

        // When
        assertDoesNotThrow(() -> userPolicyService.deleteUserPolicyCondition(username));

        // Then
        verify(memberMapper).get(username);
        verify(userPolicyMapper).deleteUserPolicyConditionById(userPolicyConditionVO.getId());
        verify(userPolicyMapper).deleteUserVectorByUserId(userId);
    }

    // ====================================
    // 맞춤형 정책 검색 테스트
    // ====================================

    @Test
    @DisplayName("맞춤형 정책 검색 - 성공")
    void searchMatchingPolicy_Success() {
        // Given
        PolicyWithVectorDTO policy = PolicyWithVectorDTO.builder()
                .policyId(1L)
                .title("청년 취업 지원")
                .endDate("상시모집")
                .vecBenefitAmount(new BigDecimal("0.7"))
                .vecDeadline(new BigDecimal("0.6"))
                .vecViews(new BigDecimal("0.5"))
                .build();
        
        when(memberMapper.get(username)).thenReturn(memberVO);
        when(userPolicyMapper.findUserPolicyConditionByUserId(userId)).thenReturn(userPolicyConditionVO);
        when(userPolicyMapper.findMatchingPoliciesExcludingApplied(any(), eq(userId)))
                .thenReturn(Arrays.asList(policy));
        when(userPolicyMapper.findUserVectorByUserId(userId)).thenReturn(userVectorVO);

        // When
        List<SearchResultDTO> result = userPolicyService.searchMatchingPolicy(username);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(memberMapper).get(username);
        verify(userPolicyMapper).findMatchingPoliciesExcludingApplied(any(), eq(userId));
    }

    // ====================================
    // 필터링된 정책 검색 테스트
    // ====================================

    // 무시된 테스트 제거됨 - searchFilteredPolicy_Success()

    // ====================================
    // 검색어 관련 테스트
    // ====================================

    @Test
    @DisplayName("인기 검색어 저장 - 성공")
    void saveSearchText_Success() {
        // Given
        String searchText = "청년 취업";
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // When
        assertDoesNotThrow(() -> userPolicyService.saveSearchText(searchText));

        // Then
        verify(redisTemplate).opsForZSet();
        verify(zSetOperations).incrementScore("popular_keywords", searchText, 1);
    }

    // 무시된 테스트 제거됨 - saveRecentSearch_Success()

    @Test
    @DisplayName("최근 검색어 조회 - 성공")
    void getRecentSearches_Success() {
        // Given
        List<String> expectedKeywords = Arrays.asList("청년 정책", "취업 지원");
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(anyString(), eq(0L), eq(5L))).thenReturn(expectedKeywords);

        // When
        List<String> result = userPolicyService.getRecentSearches(username);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedKeywords, result);
        verify(redisTemplate).opsForList();
    }

    // ====================================
    // Top3 정책 조회 테스트
    // ====================================

    @Test
    @DisplayName("Top3 정책 조회 - 성공")
    void searchTop3PoliciesByViews_Success() {
        // Given
        PolicyWithVectorDTO policy1 = PolicyWithVectorDTO.builder()
                .policyId(1L)
                .title("정책1")
                .views(1000)
                .build();
        PolicyWithVectorDTO policy2 = PolicyWithVectorDTO.builder()
                .policyId(2L)
                .title("정책2")
                .views(500)
                .build();
        
        when(memberMapper.get(username)).thenReturn(memberVO);
        when(userPolicyMapper.findUserPolicyConditionByUserId(userId)).thenReturn(userPolicyConditionVO);
        when(userPolicyMapper.findFilteredPoliciesWithVectors(any()))
                .thenReturn(Arrays.asList(policy1, policy2));

        // When
        List<SearchResultDTO> result = userPolicyService.searchTop3PoliciesByViews(username);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(memberMapper).get(username);
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(any());
    }

    @Test
    @DisplayName("전체 인기 정책 조회 - 성공")
    void searchTopPoliciesByViewsAll_Success() {
        // Given
        PolicyWithVectorDTO policy = PolicyWithVectorDTO.builder()
                .policyId(1L)
                .title("인기 정책")
                .views(2000)
                .build();
        
        when(userPolicyMapper.findFinancialPoliciesWithVectors())
                .thenReturn(Arrays.asList(policy));

        // When
        List<SearchResultDTO> result = userPolicyService.searchTopPoliciesByViewsAll(5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userPolicyMapper).findFinancialPoliciesWithVectors();
    }
}