package org.scoula.policy.service;

import lombok.extern.slf4j.Slf4j;
import org.scoula.external.gpt.GptApiClient;
import org.scoula.external.gpt.dto.GptRequestDto;
import org.scoula.external.gpt.dto.GptResponseDto;
import org.scoula.external.youthapi.YouthPolicyApiClient;
import org.scoula.policy.domain.*;
import org.scoula.policy.dto.PolicyDTO;
import org.scoula.policy.dto.YouthPolicyApiResponse;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.policy.util.PolicyMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        int dbCount = policyMapper.countAllPolicies();

        if (totalCount == dbCount) {
            log.info("[정책 수집] 변경된 정책이 없어 수집 생략됨 (API: {}, DB: {})", totalCount, dbCount);
            return;
        }

        int totalPages = 1; // 테스트용
        log.info("[정책 수집] 전체 정책 수: {}, 전체 페이지 수: {}", totalCount, totalPages);

        for (int page = 1; page <= totalPages; page++) {
            log.info("[정책 수집] {}페이지 수집 중...", page);
            YouthPolicyApiResponse response = policyApiClient.fetchPolicies(page, PAGE_SIZE);
            List<PolicyDTO> dtoList = response.getResult().getYouthPolicyList();

            int testCnt = 0;
            for (PolicyDTO dto : dtoList) {
                if (testCnt++ >= 100) break;

                // 중복 정책 건너뜀
                if (policyMapper.existsByPolicyNo(dto.getPolicyNo())) continue;

                // GPT 분석
                GptRequestDto gptRequest = new GptRequestDto(dto.getSupportContent());
                log.info("\n📤 [GPT 프롬프트 요청]\n{}", gptRequest.toPrompt());
                GptResponseDto gptResponseDto = gptApiClient.analyzePolicy(gptRequest);
                log.info("\n📥 [GPT 분석 결과]\n{{\n  \"isFinancialSupport\": {},\n  \"estimatedAmount\": {}\n}}",
                        gptResponseDto.isFinancialSupport(),
                        gptResponseDto.getEstimatedAmount());

                // VO 변환 및 분석 결과 포함
                YouthPolicyVO policyVO = PolicyMapperUtil.toYouthPolicyVO(dto);
                policyVO.setIsFinancialSupport(gptResponseDto.isFinancialSupport());
                policyVO.setPolicyBenefitAmount(gptResponseDto.getEstimatedAmount());

                policyMapper.insertPolicy(policyVO);
                Long policyId = policyVO.getId();

                // 조건 저장
                YouthPolicyConditionVO conditionVO = PolicyMapperUtil.toConditionVO(dto, policyId);
                policyMapper.insertCondition(conditionVO);

                // 운영 기간 저장
                YouthPolicyPeriodVO periodVO = PolicyMapperUtil.toPeriodVO(dto, policyId);
                policyMapper.insertPeriod(periodVO);

                // 키워드 저장 및 매핑
                List<PolicyKeywordVO> keywords = PolicyMapperUtil.toKeywordList(dto.getKeywordRaw());
                for (PolicyKeywordVO keywordVO : keywords) {
                    PolicyKeywordVO existing = policyMapper.findKeywordByName(keywordVO.getKeyword());
                    Long keywordId;
                    if (existing == null) {
                        policyMapper.insertKeyword(keywordVO);
                        keywordId = keywordVO.getId();
                    } else {
                        keywordId = existing.getId();
                    }
                    policyMapper.insertPolicyKeyword(policyId, keywordId);
                }

                // 📍 지역 코드도 키워드처럼 처리
                List<PolicyRegionVO> regionList = PolicyMapperUtil.toRegionList(dto.getRegionCode());

                for (PolicyRegionVO regionVO : regionList) {
                    PolicyRegionVO existing = policyMapper.findRegionByCode(regionVO.getRegionCode());
                    Long regionId;
                    if (existing == null) {
                        policyMapper.insertRegion(regionVO);
                        regionId = regionVO.getId(); // selectKey 사용
                    } else {
                        regionId = existing.getId();
                    }
                    policyMapper.insertPolicyRegion(policyId, regionId);
                }

            }
        }

        log.info("[정책 수집] 전체 완료");
    }
}
