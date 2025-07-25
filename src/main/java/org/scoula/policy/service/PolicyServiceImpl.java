package org.scoula.policy.service;

import lombok.extern.slf4j.Slf4j;
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
import org.scoula.policy.dto.YouthPolicyApiResponse;
import org.scoula.policy.mapper.PolicyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                // 기존 정책인 경우 조회수만 업데이트
                if (policyMapper.existsByPolicyNo(dto.getPolicyNo())) {
                    log.info("[기존 정책] 정책번호 {} 조회수 업데이트: {}", dto.getPolicyNo(), dto.getViews());
                    policyMapper.updatePolicyViews(dto.getPolicyNo(), dto.getViews());
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


            }
        }

        log.info("[정책 수집] 전체 완료");
    }
}
