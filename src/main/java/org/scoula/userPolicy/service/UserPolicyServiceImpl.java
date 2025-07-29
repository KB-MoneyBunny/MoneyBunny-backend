package org.scoula.userPolicy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.policy.domain.YouthPolicyPeriodVO;
import org.scoula.policy.domain.YouthPolicyVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.userPolicy.domain.*;
import org.scoula.userPolicy.dto.UserPolicyDTO;
import org.scoula.userPolicy.mapper.UserPolicyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPolicyServiceImpl implements UserPolicyService {

    private final UserPolicyMapper userPolicyMapper;
    private final PolicyMapper policyMapper;
    private final MemberMapper memberMapper;

    @Transactional
    @Override
    public UserPolicyDTO saveUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO) {
        MemberVO member = memberMapper.get(username);

        Long userId = member.getUserId();

        UserPolicyConditionVO condition = new UserPolicyConditionVO();
        condition.setUserId(userId);
        condition.setAge(userPolicyDTO.getAge());
        condition.setMarriage(userPolicyDTO.getMarriage());
        condition.setIncome(userPolicyDTO.getIncome());
        userPolicyMapper.saveUserPolicyCondition(condition);
        Long userPolicyConditionId = condition.getId();

        // 1. regions - from List<String> regionCodes
        if (userPolicyDTO.getRegions() != null && !userPolicyDTO.getRegions().isEmpty()) {
            List<UserRegionVO> regions = userPolicyDTO.getRegions().stream()
                    .map(code -> {
                        PolicyRegionVO policyRegion = policyMapper.findRegionByCode(code);
                        if (policyRegion != null) {
                            UserRegionVO vo = new UserRegionVO();
                            vo.setRegionId(policyRegion.getId());
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Region code {} not found", code);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!regions.isEmpty()) {
                userPolicyMapper.saveUserPolicyRegions(regions);
            }
        }

        // 2. educationLevels - from List<String>
        if (userPolicyDTO.getEducationLevels() != null && !userPolicyDTO.getEducationLevels().isEmpty()) {
            List<UserEducationLevelVO> educationLevels = userPolicyDTO.getEducationLevels().stream()
                    .map(name -> {
                        PolicyEducationLevelVO policy = policyMapper.findEducationLevelByName(name);
                        if (policy != null) {
                            UserEducationLevelVO vo = new UserEducationLevelVO();
                            vo.setEducationLevelId(policy.getId());
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Education level {} not found", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!educationLevels.isEmpty()) {
                userPolicyMapper.saveUserEducationLevels(educationLevels);
            }
        }

        // 3. employmentStatuses - from List<String>
        if (userPolicyDTO.getEmploymentStatuses() != null && !userPolicyDTO.getEmploymentStatuses().isEmpty()) {
            List<UserEmploymentStatusVO> statuses = userPolicyDTO.getEmploymentStatuses().stream()
                    .map(name -> {
                        PolicyEmploymentStatusVO policy = policyMapper.findEmploymentStatusByName(name);
                        if (policy != null) {
                            UserEmploymentStatusVO vo = new UserEmploymentStatusVO();
                            vo.setEmploymentStatusId(policy.getId());
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Employment status {} not found", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!statuses.isEmpty()) {
                userPolicyMapper.saveUserEmploymentStatuses(statuses);
            }
        }

        // 4. majors - from List<String>
        if (userPolicyDTO.getMajors() != null && !userPolicyDTO.getMajors().isEmpty()) {
            List<UserMajorVO> majors = userPolicyDTO.getMajors().stream()
                    .map(name -> {
                        PolicyMajorVO policy = policyMapper.findMajorByName(name);
                        if (policy != null) {
                            UserMajorVO vo = new UserMajorVO();
                            vo.setMajorId(policy.getId());
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Major {} not found", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!majors.isEmpty()) {
                userPolicyMapper.saveUserMajors(majors);
            }
        }

        // 5. specialConditions - from List<String>
        if (userPolicyDTO.getSpecialConditions() != null && !userPolicyDTO.getSpecialConditions().isEmpty()) {
            List<UserSpecialConditionVO> specialConditions = userPolicyDTO.getSpecialConditions().stream()
                    .map(name -> {
                        PolicySpecialConditionVO policy = policyMapper.findSpecialConditionByName(name);
                        if (policy != null) {
                            UserSpecialConditionVO vo = new UserSpecialConditionVO();
                            vo.setSpecialConditionId(policy.getId());
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Special condition {} not found", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!specialConditions.isEmpty()) {
                userPolicyMapper.saveUserSpecialConditions(specialConditions);
            }
        }

        // 6. keywords - from List<String>
        if (userPolicyDTO.getKeywords() != null && !userPolicyDTO.getKeywords().isEmpty()) {
            List<UserPolicyKeywordVO> keywords = userPolicyDTO.getKeywords().stream()
                    .map(name -> {
                        PolicyKeywordVO policy = policyMapper.findKeywordByName(name);
                        if (policy != null) {
                            UserPolicyKeywordVO vo = new UserPolicyKeywordVO();
                            vo.setKeywordId(policy.getId());
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Keyword {} not found", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!keywords.isEmpty()) {
                userPolicyMapper.saveUserPolicyKeywords(keywords);
            }
        }

        saveUserFilteredPolicies(userId);
        return userPolicyDTO;
    }

    public void saveUserFilteredPolicies(Long userId) {
        // 사용자 정책 조건 조회
        UserPolicyConditionVO userPolicyCondition = userPolicyMapper.findUserPolicyConditionByUserId(userId);
        if (userPolicyCondition == null) {
            log.error("사용자 정책 조건을 찾을 수 없습니다: userId={}", userId);
            return;
        }

        // 사용자 정책 조건에 맞는 정책 ID 목록 조회
        List<Long> matchingPolicyIds = userPolicyMapper.findMatchingPolicyIds(userPolicyCondition);
        if (matchingPolicyIds.isEmpty()) {
            log.info("사용자에게 맞는 정책이 없습니다: userId={}", userId);
            return;
        }
        List<UserFilteredPoliciesVO> filteredPolicies= new ArrayList<>();
        // 필터링된 정책 점수 저장
        for (Long policyId : matchingPolicyIds) {
            UserFilteredPoliciesVO userFilteredPolicy = UserFilteredPoliciesVO.builder()
                    .userId(userId)
                    .policyId(policyId)
                    .build();
            filteredPolicies.add(userFilteredPolicy);
        }
        userPolicyMapper.saveUserFilteredPolicies(filteredPolicies);
    }





    // 정책 점수 계산에 필요한 상수 정의(추가 논의 필요)
    private static final BigDecimal MAX_AMOUNT_THRESHOLD = new BigDecimal("10000000"); // 혜택 금액 최대치: 1천만 원
    private static final BigDecimal MAX_VIEW_THRESHOLD = new BigDecimal("10000"); // 조회수 최대치: 1만 회
    private static final long SCORE_RANGE_DAYS = 180L; // 마감일 기준 최대 범위: 180일

    /**
     * 정책에 대한 최종 점수를 계산하는 메소드.
     * 현재는 마감일 기반 점수만 계산하지만, 향후 다른 가중치 요소를 추가할 수 있습니다.
     * @param policyId 정책 ID
     * @return 계산된 최종 점수
     */
    private double calculateScoreForPolicy(Long policyId) {
        YouthPolicyPeriodVO policyPeriod = policyMapper.findYouthPolicyPeriodByPolicyId(policyId);
        YouthPolicyVO policyVO = policyMapper.findYouthPolicyById(policyId); // 다른 가중치 계산에 필요할 수 있음

        double deadlineNorm = normalizeDeadlineScore(policyPeriod);
        double amountNorm = normalizeBenefitAmount(policyVO.getPolicyBenefitAmount());
        double viewNorm = normalizeViewCount(policyVO.getViews());
        System.out.println("정책 ID: " + policyId);
        System.out.println("마감일: " + policyPeriod.getApplyPeriod());
        System.out.println("마감일 점수: " + deadlineNorm);
        System.out.println("혜택 금액: " + policyVO.getPolicyBenefitAmount());
        System.out.println("혜택 금액 점수: " + amountNorm);
        System.out.println("조회수: " + policyVO.getViews());
        System.out.println("조회수 점수: " + viewNorm);


        return deadlineNorm+amountNorm+viewNorm; // 현재는 마감일 점수만 반환
    }

    /**
     * 마감일 점수를 정규화하는 메소드.
     * 마감일까지 남은 일수에 따라 점수를 0.0에서 1.0 사이로 변환합니다.
     * @param policyPeriod 정책 기간 정보
     * @return 정규화된 마감일 점수
     */
    private double normalizeDeadlineScore(YouthPolicyPeriodVO policyPeriod) {
        if (policyPeriod == null || policyPeriod.getApplyPeriod() == null) return 0.0;

        String[] dates = policyPeriod.getApplyPeriod().split("~");
        if (dates.length != 2) return 0.0;

        try {
            String endDateStr = dates[1].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            long daysUntilEnd = ChronoUnit.DAYS.between(LocalDate.now(), endDate);

            if (daysUntilEnd <= 0) return 0.0;
            if (daysUntilEnd >= SCORE_RANGE_DAYS) return 0.0;

            return 1.0 - ((double) daysUntilEnd / SCORE_RANGE_DAYS);
        } catch (DateTimeParseException e) {
            return 0.0;
        }
    }

    /**
     * 정책의 혜택 금액을 정규화하는 메소드.
     * 혜택 금액이 1천만 원 이상이면 1.0, 그 이하이면 0.0에서 1.0 사이로 변환합니다.
     * @param policyBenefitAmount 정책의 혜택 금액
     * @return 정규화된 혜택 금액 점수
     */
    private double normalizeBenefitAmount(Long policyBenefitAmount) {
        if (policyBenefitAmount == null || policyBenefitAmount <= 0) return 0.0;

        BigDecimal amount = BigDecimal.valueOf(policyBenefitAmount);
        if (amount.compareTo(MAX_AMOUNT_THRESHOLD) >= 0) return 1.0;

        return amount.divide(MAX_AMOUNT_THRESHOLD, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 정책의 조회수를 정규화하는 메소드.
     * 조회수가 1만 회 이상이면 1.0, 그 이하이면 0.0에서 1.0 사이로 변환합니다.
     * @param viewCount 정책의 조회수
     * @return 정규화된 조회수 점수
     */
    private double normalizeViewCount(Long viewCount) {
        if (viewCount == null || viewCount <= 0) return 0.0;

        BigDecimal views = BigDecimal.valueOf(viewCount);
        if (views.compareTo(MAX_VIEW_THRESHOLD) >= 0) return 1.0;

        return views.divide(MAX_VIEW_THRESHOLD, 4, RoundingMode.HALF_UP).doubleValue();
    }
}
