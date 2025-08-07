package org.scoula.policy.service;

import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.external.gpt.GptApiClient;
import org.scoula.external.gpt.dto.GptRequestDto;
import org.scoula.external.gpt.dto.GptResponseDto;
import org.scoula.external.youthapi.YouthPolicyApiClient;
import org.scoula.policy.domain.*;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.education.YouthPolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.employment.YouthPolicyEmploymentStatusVO;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.keyword.YouthPolicyKeywordVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.major.YouthPolicyMajorVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.region.YouthPolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;
import org.scoula.policy.domain.specialcondition.YouthPolicySpecialConditionVO;
import org.scoula.policy.dto.PolicyDTO;
import org.scoula.policy.dto.PolicyDetailDTO;
import org.scoula.policy.dto.YouthPolicyApiResponse;
import org.scoula.policy.mapper.PolicyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class PolicyServiceImpl implements PolicyService {

    private static final int PAGE_SIZE = 100;

    @Autowired
    private YouthPolicyApiClient policyApiClient;

    @Autowired
    private PolicyMapper policyMapper;

    @Autowired
    private GptApiClient gptApiClient;
    
    @Autowired
    private RedisUtil redisUtil;

//  정책 벡터 설정 관련 변수
    private static final BigDecimal MAX_AMOUNT_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal MAX_VIEW_THRESHOLD = new BigDecimal("1000");
    private static final long SCORE_RANGE_DAYS = 100L;

    @Override
    @Transactional
    public void fetchAndSaveAllPolicies() {
        log.info("[정책 수집] 1페이지 호출 시작");
        YouthPolicyApiResponse firstResponse = policyApiClient.fetchPolicies(1, PAGE_SIZE);

        int totalCount = firstResponse.getResult().getPagging().getTotCount();
        int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
        log.info("[정책 수집] 전체 정책 수: {}, 전체 페이지 수: {}", totalCount, totalPages);

        for (int page = 1; page <= totalPages; page++) {
            log.info("[정책 수집] {}페이지 수집 중...", page);
            YouthPolicyApiResponse response = policyApiClient.fetchPolicies(page, PAGE_SIZE);
            List<PolicyDTO> dtoList = response.getResult().getYouthPolicyList();

            for (PolicyDTO dto : dtoList) {
                // 기존 정책인 경우 조회수, 신청URL, 신청기간만 업데이트
                if (policyMapper.existsByPolicyNo(dto.getPolicyNo())) {
                    log.info("[기존 정책] 정책번호 {} 정보 업데이트 - 조회수: {}", dto.getPolicyNo(), dto.getViews());
                    
                    // 1. 조회수 업데이트
                    policyMapper.updatePolicyViews(dto.getPolicyNo(), dto.getViews());
                    
                    // 2. 신청URL 업데이트
                    if (dto.getApplyUrl() != null && !dto.getApplyUrl().trim().isEmpty()) {
                        policyMapper.updatePolicyApplyUrl(dto.getPolicyNo(), dto.getApplyUrl());
                    }
                    
                    // 3. 신청 기간 업데이트
                    policyMapper.updatePolicyPeriod(dto.getPolicyNo(), dto.getApplyPeriod());
                    
                    // 💪 기존 정책의 정보 변경 → 벡터 재계산
                    Long policyId = policyMapper.findPolicyIdByPolicyNo(dto.getPolicyNo());
                    calculateAndSavePolicyVector(policyId);
                    continue;
                }

                // 새로운 정책 추가
                log.info("[새 정책] 정책번호 {} 저장 시작", dto.getPolicyNo());

                // GPT 분석
                GptRequestDto gptRequest = new GptRequestDto(dto.getSupportContent());
                log.info("\n📤 [GPT 프롬프트 요청]\n{}", gptRequest.toPrompt());
                GptResponseDto gptResponseDto = gptApiClient.analyzePolicy(gptRequest);
                log.info("\n📥 [GPT 분석 결과]\n{{\n  \"isFinancialSupport\": {},\n  \"estimatedAmount\": {},\n  \"policyBenefitDescription\": \"{}\"\n}}",
                        gptResponseDto.isFinancialSupport(),
                        gptResponseDto.getEstimatedAmount(),
                        gptResponseDto.getPolicyBenefitDescription());


                // VO 변환 및 분석 결과 포함
                YouthPolicyVO policyVO = YouthPolicyVO.fromDTO(dto);
                policyVO.setIsFinancialSupport(gptResponseDto.isFinancialSupport());
                policyVO.setPolicyBenefitAmount(gptResponseDto.getEstimatedAmount());
                policyVO.setPolicyBenefitDescription(gptResponseDto.getPolicyBenefitDescription());;

                policyMapper.insertPolicy(policyVO);
                Long policyId = policyVO.getId();

                // 조건 저장
                YouthPolicyConditionVO conditionVO = YouthPolicyConditionVO.fromDTO(dto, policyId);
                policyMapper.insertCondition(conditionVO);

                // 운영 기간 저장
                YouthPolicyPeriodVO periodVO = YouthPolicyPeriodVO.fromDTO(dto, policyId);
                policyMapper.insertPeriod(periodVO);

                // 키워드 저장 및 매핑
                List<PolicyKeywordVO> keywords = PolicyKeywordVO.fromCommaSeparated(dto.getKeywordRaw());
                if (keywords != null && !keywords.isEmpty()) {
                    for (PolicyKeywordVO keywordVO : keywords) {
                        PolicyKeywordVO existing = policyMapper.findKeywordByName(keywordVO.getKeyword());
                        Long keywordId;
                        if (existing == null) {
                            policyMapper.insertPolicyKeyword(keywordVO); // insertKeyword → insertPolicyKeyword
                            keywordId = keywordVO.getId();
                        } else {
                            keywordId = existing.getId();
                        }

                        YouthPolicyKeywordVO mapping = YouthPolicyKeywordVO.builder()
                                .policyId(policyId)
                                .keywordId(keywordId)
                                .createdAt(LocalDateTime.now())
                                .build();
                        policyMapper.insertYouthPolicyKeyword(mapping); //
                    }
                }


                // 지역 코드도 키워드처럼 처리
                List<PolicyRegionVO> regionList = PolicyRegionVO.fromCommaSeparated(dto.getRegionCode());
                if (regionList != null && !regionList.isEmpty()) {
                    for (PolicyRegionVO regionVO : regionList) {
                        PolicyRegionVO existing = policyMapper.findRegionByCode(regionVO.getRegionCode());
                        Long regionId;
                        if (existing == null) {
                            policyMapper.insertPolicyRegion(regionVO); // 마스터 insert (selectKey 사용)
                            regionId = regionVO.getId();
                        } else {
                            regionId = existing.getId();
                        }

                        YouthPolicyRegionVO mapping = YouthPolicyRegionVO.builder()
                                .policyId(policyId)
                                .regionId(regionId)
                                .createdAt(LocalDateTime.now())
                                .build();

                        policyMapper.insertYouthPolicyRegion(mapping); // 매핑 insert (VO 방식)
                    }
                }

                // 전공
                List<PolicyMajorVO> majorList = PolicyMajorVO.fromCommaSeparated(dto.getMajor());
                if (majorList != null && !majorList.isEmpty()) {
                    for (PolicyMajorVO majorVO : majorList) {
                        PolicyMajorVO existing = policyMapper.findMajorByName(majorVO.getMajor());
                        Long majorId;
                        if (existing == null) {
                            policyMapper.insertPolicyMajor(majorVO); // selectKey로 id 세팅됨
                            majorId = majorVO.getId();
                        } else {
                            majorId = existing.getId();
                        }

                        YouthPolicyMajorVO mapping = YouthPolicyMajorVO.builder()
                                .policyId(policyId)
                                .majorId(majorId)
                                .createdAt(LocalDateTime.now())
                                .build();
                        policyMapper.insertYouthPolicyMajor(mapping);

                    }
                }

                // 학력
                List<PolicyEducationLevelVO> eduList = PolicyEducationLevelVO.fromCommaSeparated(dto.getEducationLevel());
                if (eduList != null && !eduList.isEmpty()) {
                    for (PolicyEducationLevelVO eduVO : eduList) {
                        PolicyEducationLevelVO existing = policyMapper.findEducationLevelByName(eduVO.getEducationLevel());
                        Long eduId;
                        if (existing == null) {
                            policyMapper.insertPolicyEducationLevel(eduVO);
                            eduId = eduVO.getId();
                        } else {
                            eduId = existing.getId();
                        }

                        YouthPolicyEducationLevelVO mapping = YouthPolicyEducationLevelVO.builder()
                                .policyId(policyId)
                                .educationLevelId(eduId)
                                .createdAt(LocalDateTime.now())
                                .build();
                        policyMapper.insertYouthPolicyEducationLevel(mapping);
                    }
                }

                // 취업 상태
                List<PolicyEmploymentStatusVO> empList = PolicyEmploymentStatusVO.fromCommaSeparated(dto.getEmploymentStatus());
                if (empList != null && !empList.isEmpty()) {
                    for (PolicyEmploymentStatusVO empVO : empList) {
                        PolicyEmploymentStatusVO existing = policyMapper.findEmploymentStatusByName(empVO.getEmploymentStatus());
                        Long empId;
                        if (existing == null) {
                            policyMapper.insertPolicyEmploymentStatus(empVO);
                            empId = empVO.getId();
                        } else {
                            empId = existing.getId();
                        }

                        YouthPolicyEmploymentStatusVO mapping = YouthPolicyEmploymentStatusVO.builder()
                                .policyId(policyId)
                                .employmentStatusId(empId)
                                .createdAt(LocalDateTime.now())
                                .build();
                        policyMapper.insertYouthPolicyEmploymentStatus(mapping);
                    }
                }

                // 특수 조건
                List<PolicySpecialConditionVO> scList = PolicySpecialConditionVO.fromCommaSeparated(dto.getSpecialCondition());
                if (scList != null && !scList.isEmpty()) {
                    for (PolicySpecialConditionVO scVO : scList) {
                        PolicySpecialConditionVO existing = policyMapper.findSpecialConditionByName(scVO.getSpecialCondition());
                        Long scId;
                        if (existing == null) {
                            policyMapper.insertPolicySpecialCondition(scVO);
                            scId = scVO.getId();
                        } else {
                            scId = existing.getId();
                        }

                        YouthPolicySpecialConditionVO mapping = YouthPolicySpecialConditionVO.builder()
                                .policyId(policyId)
                                .specialConditionId(scId)
                                .createdAt(LocalDateTime.now())
                                .build();
                        policyMapper.insertYouthPolicySpecialCondition(mapping);
                    }
                }

                calculateAndSavePolicyVector(policyId);
                log.info("[새 정책] 정책번호 {} 저장 완료", dto.getPolicyNo());
            }
        }

        log.info("[정책 수집] 전체 완료");
    }

    // 💪 정책 벡터 계산 + DB 저장
    private void calculateAndSavePolicyVector(Long policyId) {
        log.info("[정책 벡터] 계산 시작 - 정책 ID: {}", policyId);
        
        YouthPolicyPeriodVO policyPeriod = policyMapper.findYouthPolicyPeriodByPolicyId(policyId);
        YouthPolicyVO policy = policyMapper.findYouthPolicyById(policyId);

        if (policy == null) {
            log.error("[정책 벡터] 정책을 찾을 수 없음 - 정책 ID: {}", policyId);
            return;
        }

        double benefitScore = normalizeBenefitAmount(policy.getPolicyBenefitAmount());
        double deadlineScore = normalizeDeadlineScore(policyPeriod);
        double viewScore = normalizeViewCount(policy.getViews());

        log.info("[정책 벡터] 점수 계산 완료 - 혜택점수: {}, 마감일점수: {}, 조회수점수: {}", 
                benefitScore, deadlineScore, viewScore);

        PolicyVectorVO vector = PolicyVectorVO.builder()
                .policyId(policyId)
                .vecBenefitAmount(BigDecimal.valueOf(benefitScore))
                .vecDeadline(BigDecimal.valueOf(deadlineScore))
                .vecViews(BigDecimal.valueOf(viewScore))
                .createdAt(LocalDateTime.now())
                .build();

        try {
            PolicyVectorVO existing = policyMapper.findByPolicyId(policyId);
            if (existing == null) {
                policyMapper.insertPolicyVector(vector);
                log.info("[정책 벡터] 신규 저장 완료 - 정책 ID: {}", policyId);
            } else {
                policyMapper.updatePolicyVector(vector);
                log.info("[정책 벡터] 업데이트 완료 - 정책 ID: {}", policyId);
            }
        } catch (Exception e) {
            log.error("[정책 벡터] DB 저장 실패 - 정책 ID: {}, 오류: {}", policyId, e.getMessage());
            e.printStackTrace();
        }
    }

    private double normalizeBenefitAmount(Long policyBenefitAmount) {
        if (policyBenefitAmount == null || policyBenefitAmount <= 0) return 0.0;
        BigDecimal amount = BigDecimal.valueOf(policyBenefitAmount);
        if (amount.compareTo(MAX_AMOUNT_THRESHOLD) >= 0) return 1.0;
        return amount.divide(MAX_AMOUNT_THRESHOLD, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private double normalizeDeadlineScore(YouthPolicyPeriodVO policyPeriod) {
        if (policyPeriod == null || policyPeriod.getApplyPeriod() == null) return 0.0;
        String[] dates = policyPeriod.getApplyPeriod().split("~");
        if (dates.length != 2) return 0.0;

        try {
            String endDateStr = dates[1].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            long daysUntilEnd = ChronoUnit.DAYS.between(LocalDate.now(), endDate);

            if (daysUntilEnd <= 0 || daysUntilEnd >= SCORE_RANGE_DAYS) return 0.0;
            return 1.0 - ((double) daysUntilEnd / SCORE_RANGE_DAYS);
        } catch (DateTimeParseException e) {
            return 0.0;
        }
    }

    private double normalizeViewCount(Long viewCount) {
        if (viewCount == null || viewCount <= 0) return 0.0;
        BigDecimal views = BigDecimal.valueOf(viewCount);
        if (views.compareTo(MAX_VIEW_THRESHOLD) >= 0) return 1.0;
        return views.divide(MAX_VIEW_THRESHOLD, 4, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public PolicyDetailDTO getPolicyById(String policyId) {
        Long id = Long.parseLong(policyId);

        // 1. 정책 기본 정보
        YouthPolicyVO policy = policyMapper.findYouthPolicyById(id);
        if (policy == null) return null;

        // 2. 조건
        YouthPolicyConditionVO condition = policyMapper.findYouthPolicyConditionByPolicyId(id);

        // 3. 기간
        YouthPolicyPeriodVO period = policyMapper.findYouthPolicyPeriodByPolicyId(id);

        // 4. 연관 정보
        List<PolicyRegionVO> regions = policyMapper.findRegionsByPolicyId(id);
        List<PolicyEducationLevelVO> educationLevels = policyMapper.findEducationLevelsByPolicyId(id);
        List<PolicyMajorVO> majors = policyMapper.findMajorsByPolicyId(id);
        List<PolicyEmploymentStatusVO> employmentStatuses = policyMapper.findEmploymentStatusesByPolicyId(id);
        List<PolicySpecialConditionVO> specialConditions = policyMapper.findSpecialConditionsByPolicyId(id);
        List<PolicyKeywordVO> keywords = policyMapper.findKeywordsByPolicyId(id);

        // 5. DTO 조립
        PolicyDetailDTO dto = new PolicyDetailDTO();
        // 정책 기본 정보
        dto.setId(policy.getId());
        dto.setPolicyNo(policy.getPolicyNo());
        dto.setTitle(policy.getTitle());
        dto.setDescription(policy.getDescription());
        dto.setSupportContent(policy.getSupportContent());
        dto.setApplicationMethod(policy.getApplicationMethod());
        dto.setScreeningMethod(policy.getScreeningMethod());
        dto.setSubmitDocuments(policy.getSubmitDocuments());
        dto.setPolicyBenefitAmount(policy.getPolicyBenefitAmount());
        dto.setEtcNotes(policy.getEtcNotes());
        dto.setApplyUrl(policy.getApplyUrl());
        dto.setRefUrl1(policy.getRefUrl1());
        dto.setRefUrl2(policy.getRefUrl2());
        dto.setIsFinancialSupport(policy.getIsFinancialSupport());
        dto.setPolicyBenefitDescription(policy.getPolicyBenefitDescription());
        dto.setView(policy.getViews());

        // 조건
        if (condition != null) {
            dto.setMinAge(condition.getMinAge());
            dto.setMaxAge(condition.getMaxAge());
            dto.setAgeLimitYn(condition.getAgeLimitYn());
            dto.setMarriageStatus(condition.getMarriageStatus());
            dto.setIncomeMin(condition.getIncomeMin());
            dto.setIncomeMax(condition.getIncomeMax());
            dto.setIncomeConditionCode(condition.getIncomeConditionCode());
            dto.setIncomeEtc(condition.getIncomeEtc());
            dto.setAdditionalConditions(condition.getAdditionalConditions());
            dto.setParticipantTarget(condition.getParticipantTarget());
        }

        // 기간
        if (period != null) {
            dto.setApplyPeriod(period.getApplyPeriod());
            dto.setBizStartDate(period.getBizStartDate());
            dto.setBizEndDate(period.getBizEndDate());
            dto.setBizPeriodEtc(period.getBizPeriodEtc());
        }

        // 연관 정보
        dto.setRegions(regions);
        dto.setEducationLevels(educationLevels);
        dto.setMajors(majors);
        dto.setEmploymentStatuses(employmentStatuses);
        dto.setSpecialConditions(specialConditions);
        dto.setKeywords(keywords);

        return dto;
    }
    
    @Override
    public PolicyDetailDTO getPolicyDetailWithTracking(String policyId, Long userId) {
        try {
            // 1. 정책 상세 정보 조회
            PolicyDetailDTO policyDetail = getPolicyById(policyId);
            if (policyDetail == null) {
                log.warn("정책을 찾을 수 없음 - policyId: {}", policyId);
                return null;
            }

            if (userId != null) {
                Long policyIdLong = Long.parseLong(policyId);
                Long dailyViewCount = redisUtil.recordDailyPolicyView(userId, policyIdLong);
                log.debug("[일일 조회 기록] userId: {}, policyId: {}, 오늘 조회수: {}",
                        userId, policyId, dailyViewCount);
            } else {
                log.debug("[익명 조회] userId 없음, Redis 기록 스킵 - policyId: {}", policyId);
            }
            
            return policyDetail;
            
        } catch (NumberFormatException e) {
            log.error("잘못된 정책 ID 형식 - policyId: {}", policyId);
            throw new IllegalArgumentException("Invalid policy ID format", e);
        } catch (Exception e) {
            log.error("정책 상세 조회 실패 - policyId: {}, 오류: {}", policyId, e.getMessage());
            throw new RuntimeException("Policy detail retrieval failed", e);
        }
    }
}
