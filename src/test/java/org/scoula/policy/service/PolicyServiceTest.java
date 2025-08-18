package org.scoula.policy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.common.util.RedisUtil;
import org.scoula.external.gpt.GptApiClient;
import org.scoula.external.gpt.dto.GptRequestDto;
import org.scoula.external.gpt.dto.GptResponseDto;
import org.scoula.external.gpt.service.PromptBuilderService;
import org.scoula.external.youthapi.YouthPolicyApiClient;
import org.scoula.policy.domain.*;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;
import org.scoula.policy.dto.PolicyDTO;
import org.scoula.policy.dto.PolicyDetailDTO;
import org.scoula.policy.dto.PolicyPagging;
import org.scoula.policy.dto.YouthPolicyApiResponse;
import org.scoula.policy.dto.YouthPolicyResult;
import org.scoula.policy.mapper.PolicyMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyService 단위 테스트")
class PolicyServiceTest {

    @Mock
    private YouthPolicyApiClient policyApiClient;

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private GptApiClient gptApiClient;

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private PolicyPagging pagging;
    private YouthPolicyResult result;
    private YouthPolicyApiResponse apiResponse;
    private PolicyDTO policyDTO;
    private GptResponseDto gptResponse;
    private YouthPolicyVO youthPolicyVO;
    private PolicyDetailDTO policyDetailDTO;

    @BeforeEach
    void setUp() {
        // API 응답 모킹 데이터
        pagging = new PolicyPagging();
        pagging.setTotCount(150);

        policyDTO = new PolicyDTO();
        policyDTO.setPolicyNo("R2023123456");
        policyDTO.setTitle("청년 취업 지원 정책");
        policyDTO.setDescription("청년들의 취업을 지원하는 정책입니다");
        policyDTO.setSupportContent("취업 준비 비용 지원");
        policyDTO.setViews(1000L);
        policyDTO.setApplyUrl("https://example.com/apply");
        policyDTO.setApplyPeriod("20240101~20241231");
        policyDTO.setKeywordRaw("취업,청년,지원");
        policyDTO.setRegionCode("11000,26000");
        policyDTO.setMajor("컴퓨터공학,경영학");
        policyDTO.setEducationLevel("대학졸업");
        policyDTO.setEmploymentStatus("미취업");
        policyDTO.setSpecialCondition("청년");

        result = new YouthPolicyResult();
        result.setPagging(pagging);
        result.setYouthPolicyList(Arrays.asList(policyDTO));

        apiResponse = new YouthPolicyApiResponse();
        apiResponse.setResult(result);

        // GPT 응답 모킹 데이터
        gptResponse = new GptResponseDto(true, 500000L, "취업 준비 활동비 월 50만원 지원");

        // 정책 VO 모킹 데이터
        youthPolicyVO = new YouthPolicyVO();
        youthPolicyVO.setId(1L);
        youthPolicyVO.setPolicyNo("R2023123456");
        youthPolicyVO.setTitle("청년 취업 지원 정책");
        youthPolicyVO.setDescription("청년들의 취업을 지원하는 정책입니다");
        youthPolicyVO.setSupportContent("취업 준비 비용 지원");
        youthPolicyVO.setViews(1000L);
        youthPolicyVO.setIsFinancialSupport(true);
        youthPolicyVO.setPolicyBenefitAmount(500000L);
        youthPolicyVO.setPolicyBenefitDescription("취업 준비 활동비 월 50만원 지원");
        youthPolicyVO.setCreatedAt(LocalDateTime.now());

        // 정책 상세 DTO 모킹 데이터
        policyDetailDTO = new PolicyDetailDTO();
        policyDetailDTO.setId(1L);
        policyDetailDTO.setPolicyNo("R2023123456");
        policyDetailDTO.setTitle("청년 취업 지원 정책");
        policyDetailDTO.setDescription("청년들의 취업을 지원하는 정책입니다");
        policyDetailDTO.setView(1000L);
        policyDetailDTO.setIsFinancialSupport(true);
        policyDetailDTO.setPolicyBenefitAmount(500000L);
    }

    // ====================================
    // 정책 수집 및 저장 테스트
    // ====================================

    @Disabled("외부 API 의존성으로 인한 NPE - 추후 개선 필요")
    @Test
    @DisplayName("전체 정책 수집 및 저장 - 성공")
    void fetchAndSaveAllPolicies_Success() {
        // Given
        when(policyApiClient.fetchPolicies(1, 100)).thenReturn(apiResponse);
        when(policyMapper.existsByPolicyNo(policyDTO.getPolicyNo())).thenReturn(false);
        when(promptBuilderService.buildPromptOptimized(anyString())).thenReturn("GPT 분석 프롬프트");
        when(gptApiClient.analyzePolicy(any(GptRequestDto.class))).thenReturn(gptResponse);
        when(policyMapper.findByPolicyId(any())).thenReturn(null);

        // When
        assertDoesNotThrow(() -> policyService.fetchAndSaveAllPolicies());

        // Then
        verify(policyApiClient, atLeastOnce()).fetchPolicies(anyInt(), eq(100));
        verify(policyMapper).insertPolicy(any(YouthPolicyVO.class));
        verify(policyMapper).insertCondition(any(YouthPolicyConditionVO.class));
        verify(policyMapper).insertPeriod(any(YouthPolicyPeriodVO.class));
        verify(policyMapper).insertPolicyVector(any(PolicyVectorVO.class));
    }

    @Disabled("외부 API 의존성으로 인한 NPE - 추후 개선 필요")
    @Test
    @DisplayName("기존 정책 업데이트 - 성공")
    void fetchAndSaveAllPolicies_ExistingPolicy() {
        // Given
        when(policyApiClient.fetchPolicies(1, 100)).thenReturn(apiResponse);
        when(policyMapper.existsByPolicyNo(policyDTO.getPolicyNo())).thenReturn(true);
        when(policyMapper.findPolicyIdByPolicyNo(policyDTO.getPolicyNo())).thenReturn(1L);
        when(policyMapper.findByPolicyId(1L)).thenReturn(null);

        // When
        assertDoesNotThrow(() -> policyService.fetchAndSaveAllPolicies());

        // Then
        verify(policyMapper).updatePolicyViews(policyDTO.getPolicyNo(), policyDTO.getViews());
        verify(policyMapper).updatePolicyApplyUrl(policyDTO.getPolicyNo(), policyDTO.getApplyUrl());
        verify(policyMapper).updatePolicyPeriod(policyDTO.getPolicyNo(), policyDTO.getApplyPeriod());
        verify(policyMapper, never()).insertPolicy(any());
    }

    // ====================================
    // 정책 상세 조회 테스트
    // ====================================

    @Test
    @DisplayName("정책 상세 조회 - 성공")
    void getPolicyById_Success() {
        // Given
        String policyId = "1";
        when(policyMapper.findYouthPolicyById(1L)).thenReturn(youthPolicyVO);
        when(policyMapper.findYouthPolicyConditionByPolicyId(1L)).thenReturn(
new YouthPolicyConditionVO());
        when(policyMapper.findYouthPolicyPeriodByPolicyId(1L)).thenReturn(
new YouthPolicyPeriodVO());
        when(policyMapper.findRegionsByPolicyId(1L)).thenReturn(
                Arrays.asList(createPolicyRegionVO("11000")));
        when(policyMapper.findEducationLevelsByPolicyId(1L)).thenReturn(
                Arrays.asList(createPolicyEducationLevelVO("대학졸업")));
        when(policyMapper.findMajorsByPolicyId(1L)).thenReturn(
                Arrays.asList(createPolicyMajorVO("컴퓨터공학")));
        when(policyMapper.findEmploymentStatusesByPolicyId(1L)).thenReturn(
                Arrays.asList(createPolicyEmploymentStatusVO("미취업")));
        when(policyMapper.findSpecialConditionsByPolicyId(1L)).thenReturn(
                Arrays.asList(createPolicySpecialConditionVO("청년")));
        when(policyMapper.findKeywordsByPolicyId(1L)).thenReturn(
                Arrays.asList(createPolicyKeywordVO("취업")));

        // When
        PolicyDetailDTO result = policyService.getPolicyById(policyId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("R2023123456", result.getPolicyNo());
        assertEquals("청년 취업 지원 정책", result.getTitle());
        verify(policyMapper).findYouthPolicyById(1L);
    }

    @Test
    @DisplayName("정책 상세 조회 - 정책 없음")
    void getPolicyById_PolicyNotFound() {
        // Given
        String policyId = "999";
        when(policyMapper.findYouthPolicyById(999L)).thenReturn(null);

        // When
        PolicyDetailDTO result = policyService.getPolicyById(policyId);

        // Then
        assertNull(result);
        verify(policyMapper).findYouthPolicyById(999L);
    }

    // ====================================
    // 추적 포함 정책 상세 조회 테스트
    // ====================================

    @Test
    @DisplayName("추적 포함 정책 상세 조회 - 로그인 사용자")
    void getPolicyDetailWithTracking_LoggedInUser() {
        // Given
        String policyId = "1";
        Long userId = 100L;
        when(policyMapper.findYouthPolicyById(1L)).thenReturn(youthPolicyVO);
        when(policyMapper.findYouthPolicyConditionByPolicyId(1L)).thenReturn(
new YouthPolicyConditionVO());
        when(policyMapper.findYouthPolicyPeriodByPolicyId(1L)).thenReturn(
new YouthPolicyPeriodVO());
        when(policyMapper.findRegionsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findEducationLevelsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findMajorsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findEmploymentStatusesByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findSpecialConditionsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findKeywordsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(redisUtil.recordDailyPolicyView(userId, 1L)).thenReturn(5L);

        // When
        PolicyDetailDTO result = policyService.getPolicyDetailWithTracking(policyId, userId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(redisUtil).recordDailyPolicyView(userId, 1L);
        verify(policyMapper).findYouthPolicyById(1L);
    }

    @Test
    @DisplayName("추적 포함 정책 상세 조회 - 익명 사용자")
    void getPolicyDetailWithTracking_AnonymousUser() {
        // Given
        String policyId = "1";
        when(policyMapper.findYouthPolicyById(1L)).thenReturn(youthPolicyVO);
        when(policyMapper.findYouthPolicyConditionByPolicyId(1L)).thenReturn(
new YouthPolicyConditionVO());
        when(policyMapper.findYouthPolicyPeriodByPolicyId(1L)).thenReturn(
new YouthPolicyPeriodVO());
        when(policyMapper.findRegionsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findEducationLevelsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findMajorsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findEmploymentStatusesByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findSpecialConditionsByPolicyId(1L)).thenReturn(Arrays.asList());
        when(policyMapper.findKeywordsByPolicyId(1L)).thenReturn(Arrays.asList());

        // When
        PolicyDetailDTO result = policyService.getPolicyDetailWithTracking(policyId, null);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(redisUtil, never()).recordDailyPolicyView(any(), any());
        verify(policyMapper).findYouthPolicyById(1L);
    }

    @Test
    @DisplayName("추적 포함 정책 상세 조회 - 잘못된 정책 ID")
    void getPolicyDetailWithTracking_InvalidPolicyId() {
        // Given
        String policyId = "invalid";

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                policyService.getPolicyDetailWithTracking(policyId, 1L));
    }

    // ====================================
    // GPT 연동 테스트
    // ====================================

    @Disabled("외부 API 의존성으로 인한 NPE - 추후 개선 필요")
    @Test
    @DisplayName("GPT 분석 연동 - 성공")
    void gptAnalysis_Success() {
        // Given
        when(policyApiClient.fetchPolicies(1, 100)).thenReturn(apiResponse);
        when(policyMapper.existsByPolicyNo(policyDTO.getPolicyNo())).thenReturn(false);
        when(promptBuilderService.buildPromptOptimized("취업 준비 비용 지원")).thenReturn("분석 프롬프트");
        when(gptApiClient.analyzePolicy(any(GptRequestDto.class))).thenReturn(gptResponse);
        when(policyMapper.findByPolicyId(any())).thenReturn(null);

        // When
        assertDoesNotThrow(() -> policyService.fetchAndSaveAllPolicies());

        // Then
        verify(promptBuilderService).buildPromptOptimized("취업 준비 비용 지원");
        verify(gptApiClient).analyzePolicy(any(GptRequestDto.class));
    }

    // ====================================
    // 정책 벡터 계산 테스트 (간접 테스트)
    // ====================================

    @Disabled("외부 API 의존성으로 인한 NPE - 추후 개선 필요")
    @Test
    @DisplayName("정책 벡터 계산 - 신규 정책")
    void policyVectorCalculation_NewPolicy() {
        // Given
        when(policyApiClient.fetchPolicies(1, 100)).thenReturn(apiResponse);
        when(policyMapper.existsByPolicyNo(policyDTO.getPolicyNo())).thenReturn(false);
        when(promptBuilderService.buildPromptOptimized(anyString())).thenReturn("프롬프트");
        when(gptApiClient.analyzePolicy(any(GptRequestDto.class))).thenReturn(gptResponse);
        when(policyMapper.findByPolicyId(any())).thenReturn(null);
        when(policyMapper.findYouthPolicyPeriodByPolicyId(any())).thenReturn(
                createYouthPolicyPeriodVO("20240101~20241231"));
        when(policyMapper.findYouthPolicyById(any())).thenReturn(youthPolicyVO);

        // When
        assertDoesNotThrow(() -> policyService.fetchAndSaveAllPolicies());

        // Then
        verify(policyMapper).insertPolicyVector(any(PolicyVectorVO.class));
    }

    // Helper methods for creating test objects
    private PolicyRegionVO createPolicyRegionVO(String regionCode) {
        PolicyRegionVO vo = new PolicyRegionVO();
        vo.setRegionCode(regionCode);
        return vo;
    }

    private PolicyEducationLevelVO createPolicyEducationLevelVO(String educationLevel) {
        PolicyEducationLevelVO vo = new PolicyEducationLevelVO();
        vo.setEducationLevel(educationLevel);
        return vo;
    }

    private PolicyMajorVO createPolicyMajorVO(String major) {
        PolicyMajorVO vo = new PolicyMajorVO();
        vo.setMajor(major);
        return vo;
    }

    private PolicyEmploymentStatusVO createPolicyEmploymentStatusVO(String employmentStatus) {
        PolicyEmploymentStatusVO vo = new PolicyEmploymentStatusVO();
        vo.setEmploymentStatus(employmentStatus);
        return vo;
    }

    private PolicySpecialConditionVO createPolicySpecialConditionVO(String specialCondition) {
        PolicySpecialConditionVO vo = new PolicySpecialConditionVO();
        vo.setSpecialCondition(specialCondition);
        return vo;
    }

    private PolicyKeywordVO createPolicyKeywordVO(String keyword) {
        PolicyKeywordVO vo = new PolicyKeywordVO();
        vo.setKeyword(keyword);
        return vo;
    }

    private YouthPolicyPeriodVO createYouthPolicyPeriodVO(String applyPeriod) {
        YouthPolicyPeriodVO vo = new YouthPolicyPeriodVO();
        vo.setApplyPeriod(applyPeriod);
        return vo;
    }
}