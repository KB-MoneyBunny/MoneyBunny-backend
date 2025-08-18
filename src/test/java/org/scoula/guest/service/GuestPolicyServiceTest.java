package org.scoula.guest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.policy.util.PolicyDataHolder;
import org.scoula.userPolicy.dto.PolicyWithVectorDTO;
import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.mapper.UserPolicyMapper;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuestPolicyService 단위 테스트")
class GuestPolicyServiceTest {

    @Mock
    private UserPolicyMapper userPolicyMapper;

    @Mock
    private PolicyDataHolder policyDataHolder;

    @InjectMocks
    private GuestPolicyServiceImpl guestPolicyService;

    private SearchRequestDTO searchRequestDTO;
    private PolicyWithVectorDTO policyWithVectorDTO;

    @BeforeEach
    void setUp() {
        searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setRegions(Arrays.asList("11000", "26000"));
        searchRequestDTO.setEducationLevels(Arrays.asList("대학졸업"));
        searchRequestDTO.setEmploymentStatuses(Arrays.asList("미취업"));
        searchRequestDTO.setMajors(Arrays.asList("컴퓨터공학"));
        searchRequestDTO.setSpecialConditions(Arrays.asList("청년"));
        searchRequestDTO.setKeywords(Arrays.asList("취업", "창업"));
        searchRequestDTO.setAge(25);
        searchRequestDTO.setIncome(30000000L);

        policyWithVectorDTO = new PolicyWithVectorDTO();
        policyWithVectorDTO.setPolicyId(1L);
        policyWithVectorDTO.setTitle("청년 취업 지원 정책");
        policyWithVectorDTO.setPolicyBenefitDescription("취업 준비 비용 지원");
        policyWithVectorDTO.setViews(1000);
        policyWithVectorDTO.setPolicyBenefitAmount(500000L);
        policyWithVectorDTO.setVecBenefitAmount(new BigDecimal("0.8"));
        policyWithVectorDTO.setVecDeadline(new BigDecimal("0.6"));
        policyWithVectorDTO.setVecViews(new BigDecimal("0.7"));
    }

    // ====================================
    // 비회원 정책 검색 테스트
    // ====================================

    @Test
    @DisplayName("비회원 정책 검색 - 성공")
    void searchGuestPolicies_Success() {
        // Given
        List<PolicyWithVectorDTO> mockPolicies = Arrays.asList(policyWithVectorDTO);
        when(userPolicyMapper.findFilteredPoliciesWithVectors(any(SearchRequestDTO.class)))
                .thenReturn(mockPolicies);
        when(policyDataHolder.getRegionCodesByPrefix("11"))
                .thenReturn(Arrays.asList("11000", "11010", "11020"));
        when(policyDataHolder.getRegionCodesByPrefix("26"))
                .thenReturn(Arrays.asList("26000", "26010", "26020"));

        // When
        List<SearchResultDTO> result = guestPolicyService.searchGuestPolicies(searchRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        SearchResultDTO resultDTO = result.get(0);
        assertEquals(1L, resultDTO.getPolicyId());
        assertEquals("청년 취업 지원 정책", resultDTO.getTitle());
        assertEquals("취업 준비 비용 지원", resultDTO.getPolicyBenefitDescription());
        assertEquals(500000L, resultDTO.getPolicyBenefitAmount());
        
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(any(SearchRequestDTO.class));
        verify(policyDataHolder).getRegionCodesByPrefix("11");
        verify(policyDataHolder).getRegionCodesByPrefix("26");
    }

    @Test
    @DisplayName("비회원 정책 검색 - 빈 결과")
    void searchGuestPolicies_EmptyResult() {
        // Given
        when(userPolicyMapper.findFilteredPoliciesWithVectors(any(SearchRequestDTO.class)))
                .thenReturn(Collections.emptyList());
        when(policyDataHolder.getRegionCodesByPrefix("11"))
                .thenReturn(Arrays.asList("11000"));
        when(policyDataHolder.getRegionCodesByPrefix("26"))
                .thenReturn(Arrays.asList("26000"));

        // When
        List<SearchResultDTO> result = guestPolicyService.searchGuestPolicies(searchRequestDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(any(SearchRequestDTO.class));
    }

    @Test
    @DisplayName("비회원 정책 검색 - 빈 문자열 필터링")
    void searchGuestPolicies_FilterEmptyStrings() {
        // Given
        searchRequestDTO.setRegions(Arrays.asList("11000", "", "26000", "   "));
        searchRequestDTO.setEducationLevels(Arrays.asList("대학졸업", "", "고등학교졸업"));
        searchRequestDTO.setEmploymentStatuses(Arrays.asList("", "미취업"));
        searchRequestDTO.setMajors(Arrays.asList("컴퓨터공학", "   ", "경영학"));
        searchRequestDTO.setSpecialConditions(Arrays.asList("청년", ""));
        searchRequestDTO.setKeywords(Arrays.asList("취업", "", "창업"));

        when(userPolicyMapper.findFilteredPoliciesWithVectors(any(SearchRequestDTO.class)))
                .thenReturn(Arrays.asList(policyWithVectorDTO));
        when(policyDataHolder.getRegionCodesByPrefix("11"))
                .thenReturn(Arrays.asList("11000"));
        when(policyDataHolder.getRegionCodesByPrefix("26"))
                .thenReturn(Arrays.asList("26000"));

        // When
        List<SearchResultDTO> result = guestPolicyService.searchGuestPolicies(searchRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(argThat(request -> {
            // 빈 문자열이 제거되고 지역 코드가 확장되었는지 확인
            List<String> regions = request.getRegions();
            return regions != null && regions.size() >= 2 && // 11000, 26000 + 확장된 지역들
                   request.getEducationLevels().size() == 2 &&
                   request.getEmploymentStatuses().size() == 1 &&
                   request.getMajors().size() == 2 &&
                   request.getSpecialConditions().size() == 1 &&
                   request.getKeywords().size() == 2;
        }));
    }

    @Test
    @DisplayName("비회원 정책 검색 - null 지역 코드로 인한 예외 처리")
    void searchGuestPolicies_HandleNullRegions() {
        // Given
        SearchRequestDTO nullRegionRequest = new SearchRequestDTO();
        nullRegionRequest.setRegions(null); // 이 경우 NullPointerException 발생 예상
        nullRegionRequest.setEducationLevels(Arrays.asList("대학졸업"));
        nullRegionRequest.setEmploymentStatuses(Arrays.asList("미취업"));
        nullRegionRequest.setMajors(Arrays.asList("컴퓨터공학"));
        nullRegionRequest.setSpecialConditions(Arrays.asList("청년"));
        nullRegionRequest.setKeywords(Arrays.asList("취업", "창업"));

        // When & Then - null regions로 인해 NullPointerException이 발생해야 함
        assertThrows(NullPointerException.class, () -> {
            guestPolicyService.searchGuestPolicies(nullRegionRequest);
        });
    }

    @Test
    @DisplayName("비회원 정책 검색 - 지역 코드 확장 테스트")
    void searchGuestPolicies_RegionCodeExpansion() {
        // Given
        searchRequestDTO.setRegions(Arrays.asList("11000")); // 서울 광역시
        when(userPolicyMapper.findFilteredPoliciesWithVectors(any(SearchRequestDTO.class)))
                .thenReturn(Arrays.asList(policyWithVectorDTO));
        when(policyDataHolder.getRegionCodesByPrefix("11"))
                .thenReturn(Arrays.asList("11010", "11020", "11030")); // 서울 구별 코드들

        // When
        List<SearchResultDTO> result = guestPolicyService.searchGuestPolicies(searchRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(policyDataHolder).getRegionCodesByPrefix("11");
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(argThat(request -> {
            // 원본 지역코드 + 확장된 지역코드들이 포함되어야 함
            List<String> regions = request.getRegions();
            return regions.contains("11000") && 
                   regions.contains("11010") && 
                   regions.contains("11020") && 
                   regions.contains("11030");
        }));
    }

    @Test
    @DisplayName("비회원 정책 검색 - 일반화된 지역 코드 추가")
    void searchGuestPolicies_GeneralizedRegionCode() {
        // Given
        searchRequestDTO.setRegions(Arrays.asList("11010")); // 서울 종로구
        when(userPolicyMapper.findFilteredPoliciesWithVectors(any(SearchRequestDTO.class)))
                .thenReturn(Arrays.asList(policyWithVectorDTO));
        // 11010은 00 끝나지 않으므로 prefix 조회 없음

        // When
        List<SearchResultDTO> result = guestPolicyService.searchGuestPolicies(searchRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(argThat(request -> {
            List<String> regions = request.getRegions();
            // 원본(11010) + 일반화된 코드(11000)가 포함되어야 함
            return regions.contains("11010") && regions.contains("11000");
        }));
        // 11010은 000으로 끝나지 않으므로 prefix 조회가 호출되지 않음
        verify(policyDataHolder, never()).getRegionCodesByPrefix(anyString());
    }

    @Test
    @DisplayName("비회원 정책 검색 - 복수 정책 결과")
    void searchGuestPolicies_MultiplePolicies() {
        // Given
        PolicyWithVectorDTO policy2 = new PolicyWithVectorDTO();
        policy2.setPolicyId(2L);
        policy2.setTitle("청년 창업 지원 정책");
        policy2.setPolicyBenefitDescription("창업 자금 지원");
        policy2.setViews(500);
        policy2.setPolicyBenefitAmount(1000000L);
        policy2.setVecBenefitAmount(new BigDecimal("0.9"));
        policy2.setVecDeadline(new BigDecimal("0.5"));
        policy2.setVecViews(new BigDecimal("0.4"));

        List<PolicyWithVectorDTO> mockPolicies = Arrays.asList(policyWithVectorDTO, policy2);
        when(userPolicyMapper.findFilteredPoliciesWithVectors(any(SearchRequestDTO.class)))
                .thenReturn(mockPolicies);
        when(policyDataHolder.getRegionCodesByPrefix("11"))
                .thenReturn(Arrays.asList("11000"));
        when(policyDataHolder.getRegionCodesByPrefix("26"))
                .thenReturn(Arrays.asList("26000"));

        // When
        List<SearchResultDTO> result = guestPolicyService.searchGuestPolicies(searchRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        SearchResultDTO firstResult = result.get(0);
        assertEquals("청년 취업 지원 정책", firstResult.getTitle());
        assertEquals(500000L, firstResult.getPolicyBenefitAmount());
        
        SearchResultDTO secondResult = result.get(1);
        assertEquals("청년 창업 지원 정책", secondResult.getTitle());
        assertEquals(1000000L, secondResult.getPolicyBenefitAmount());
        
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(any(SearchRequestDTO.class));
    }

    @Test
    @DisplayName("비회원 정책 검색 - 매퍼 예외 처리")
    void searchGuestPolicies_MapperException() {
        // Given
        when(userPolicyMapper.findFilteredPoliciesWithVectors(any(SearchRequestDTO.class)))
                .thenThrow(new RuntimeException("Database connection error"));
        when(policyDataHolder.getRegionCodesByPrefix("11"))
                .thenReturn(Arrays.asList("11000"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            guestPolicyService.searchGuestPolicies(searchRequestDTO);
        });
        
        verify(userPolicyMapper).findFilteredPoliciesWithVectors(any(SearchRequestDTO.class));
    }
}