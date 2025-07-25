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

        return userPolicyDTO;
    }

    @Transactional
    @Override
    public void saveUserPolicyScore(String username) {
        // 사용자 정보 조회
        MemberVO member = memberMapper.get(username);
        if (member == null) {
            log.error("사용자를 찾을 수 없습니다: {}", username);
            return;
        }

        // 사용자 정책 조건 조회
        Long userId = member.getUserId();
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

        // 사용자의 각 정책별 점수 계산 및 저장
        for (Long policyId : matchingPolicyIds) {
            BigDecimal score = calculateScoreForPolicy(policyId);
            System.out.println("점수"+score);
            System.out.println(policyId);

//            UserPolicyScoreVO scoreVO = UserPolicyScoreVO.builder()
//                    .userId(userId)
//                    .policyId(policyId)
//                    .score(score)
//                    .build();
//            userPolicyMapper.saveUserPolicyScore(scoreVO);
        }
    }

    // 최소 최대 점수가 0~10으로 설정되어 있습니다.
    // 점수 계산 로직은 남은 정책 기간에 따라 선형 보간을 사용합니다.
    // 남은 기간이 많을수록 점수가 낮아지고, 남은 기간이 적을수록 점수가 높아집니다.
    // 최대 10점에서 최소 0점까지 선형적으로 감소합니다.
    // 남은 기간이 365일 이상이면 최소 점수인 0점을 반환합니다.
    // 남은 기간이 0일 이하이면 0점을 반환합니다.
    private static final BigDecimal MAX_SCORE = new BigDecimal("10"); // 최대 점수
    private static final BigDecimal MIN_SCORE = new BigDecimal("0"); // 최소 점수
    private static final int SCORE_RANGE_DAYS = 365; // 점수 계산 대상 일 수


    /**
     * 정책에 대한 최종 점수를 계산하는 메소드.
     * 현재는 마감일 기반 점수만 계산하지만, 향후 다른 가중치 요소를 추가할 수 있습니다.
     * @param policyId 정책 ID
     * @return 계산된 최종 점수
     */
    private BigDecimal calculateScoreForPolicy(Long policyId) {
        YouthPolicyPeriodVO policyPeriod = policyMapper.findYouthPolicyPeriodByPolicyId(policyId);
        YouthPolicyVO policyVO = policyMapper.findYouthPolicyById(policyId); // 다른 가중치 계산에 필요할 수 있음
        ;
        // 1. 마감일 기반 점수 계산
        BigDecimal deadlineScore = calculateDeadlineScore(policyPeriod);

        // 2. 혜택 금액 기반 점수 계산
        System.out.println("정책 혜택 금액: " + policyVO.getPolicyBenefitAmount());
        BigDecimal benefitAmountScore = calculateBenefitAmountScore(policyVO.getPolicyBenefitAmount());

        // 3. 조회수 기반 점수 계산
        System.out.println("정책 조회수: " + policyVO.getView());
        BigDecimal viewCountScore = calculateViewScore(policyVO.getView());

        // 최종 점수 = 각 가중치 점수의 합

        return deadlineScore.add(benefitAmountScore).add(viewCountScore); // 현재는 마감일 점수만 반환
    }

    /**
     * 정책의 마감일까지 남은 기간을 기반으로 점수를 계산하는 메소드.
     * @param policyPeriod 정책 기간 정보
     * @return 마감일 기반 점수
     */
    private BigDecimal calculateDeadlineScore(YouthPolicyPeriodVO policyPeriod) {
        if (policyPeriod == null || policyPeriod.getBizEndDate() == null) {
            return MIN_SCORE;
        }

        String applyPeriod = policyPeriod.getApplyPeriod();
        String[] dates = applyPeriod.split("~");
        if (dates.length != 2) {
            return MIN_SCORE; // 예상한 형식이 아닐 경우 최소 점수
        }
        String endDateStr = dates[1].trim();
        System.out.println("마감 기간: " + endDateStr);
        LocalDate endDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            endDate = LocalDate.parse(endDateStr, formatter); // 기본 ISO 포맷 (yyyy-MM-dd)
            System.out.println("파싱된 마감 날짜: " + endDate);
        } catch (DateTimeParseException e) {
            log.warn("지원 기간 파싱 실패: {}", applyPeriod);
            return MIN_SCORE;
        }

        long daysUntilEnd = ChronoUnit.DAYS.between(LocalDate.now(), endDate);

        if (daysUntilEnd <= 0) {
            return BigDecimal.ZERO;
        }

        if (daysUntilEnd >= SCORE_RANGE_DAYS) {
            return MIN_SCORE; // 너무 멀면 최소 점수
        }

        // 선형 보간: 남은 일수 적을수록 높은 점수
        BigDecimal delta = MAX_SCORE.subtract(MIN_SCORE);
        BigDecimal score = MAX_SCORE.subtract(
                delta.multiply(BigDecimal.valueOf(daysUntilEnd))
                        .divide(BigDecimal.valueOf(SCORE_RANGE_DAYS), 4, RoundingMode.HALF_UP)
        );

        return score.setScale(2, RoundingMode.HALF_UP);
    }


    private static final BigDecimal MAX_AMOUNT_THRESHOLD = new BigDecimal("10000000"); // 1천만 원

    private BigDecimal calculateBenefitAmountScore(Long policyBenefitAmount) {
        if (policyBenefitAmount == null || policyBenefitAmount <= 0) {
            return MIN_SCORE;
        }

        BigDecimal amount = BigDecimal.valueOf(policyBenefitAmount);

        if (amount.compareTo(MAX_AMOUNT_THRESHOLD) >= 0) {
            return MAX_SCORE;
        }

        // 선형 보간 계산
        BigDecimal ratio = amount
                .divide(MAX_AMOUNT_THRESHOLD, 4, RoundingMode.HALF_UP);

        BigDecimal score = MIN_SCORE.add(
                MAX_SCORE.subtract(MIN_SCORE).multiply(ratio)
        );

        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateViewScore(Long viewCount) {
        if (viewCount == null || viewCount <= 0) {
            return MIN_SCORE;
        }

        // 조회수에 따라 점수를 계산
        // 예시: 조회수가 많을수록 점수가 높아짐
        BigDecimal viewScore = new BigDecimal(viewCount)
                .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP); // 1000회당 1점

        // 최대 점수는 10점으로 제한
        if (viewScore.compareTo(MAX_SCORE) > 0) {
            return MAX_SCORE;
        }

        return viewScore.setScale(2, RoundingMode.HALF_UP);
    }
}
