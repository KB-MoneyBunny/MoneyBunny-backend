package org.scoula.userPolicy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.policy.util.PolicyDataHolder;
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
import org.scoula.userPolicy.util.VectorUtil;
import org.scoula.userPolicy.domain.UserVectorVO;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPolicyServiceImpl implements UserPolicyService {

    private final UserPolicyMapper userPolicyMapper;
    private final MemberMapper memberMapper;
    private final PolicyDataHolder policyDataHolder;
    private final RedisUtil redisUtil;
    private final RedisTemplate<String, String> redisTemplate;


    private static final String POPULAR_KEYWORDS_KEY = "popular_keywords";
    private static final String RECENT_KEYWORDS_KEY_PREFIX = "recent_keywords:";
    private static final int MAX_RECENT_KEYWORDS = 6;
    private final PolicyInteractionMapper policyInteractionMapper;


    /**
     * 사용자 정책 조건을 조회합니다.
     * @param username 사용자 이름
     * @return 사용자 정책 DTO
     */
    @Override
    public TestResultRequestDTO getUserPolicyCondition(String username) {


        MemberVO member = memberMapper.get(username);
        if (member == null) {
            log.error("사용자를 찾을 수 없습니다: username={}", username);
            return null; // Or throw an exception
        }

        Long userId = member.getUserId();
        UserPolicyConditionVO userPolicyCondition = userPolicyMapper.findUserPolicyConditionByUserId(userId);
        if (userPolicyCondition == null) {
            log.info("사용자 정책 조건이 존재하지 않습니다: userId={}", userId);
            return null; // Or throw an exception
        }

        TestResultRequestDTO testResultRequestDTO = new TestResultRequestDTO();
        testResultRequestDTO.setAge(userPolicyCondition.getAge());
        testResultRequestDTO.setMarriage(userPolicyCondition.getMarriage());
        testResultRequestDTO.setIncome(userPolicyCondition.getIncome());
        testResultRequestDTO.setMoneyRank(userPolicyCondition.getMoneyRank());
        testResultRequestDTO.setPeriodRank(userPolicyCondition.getPeriodRank());
        testResultRequestDTO.setPopularityRank(userPolicyCondition.getPopularityRank());

        // 1. regions
        List<UserRegionVO> regions = userPolicyCondition.getRegions();
        if (regions != null && !regions.isEmpty()) {
            List<String> regionCodes = regions.stream()
                    .map(region -> policyDataHolder.getRegionName(region.getRegionId()))
                    .collect(Collectors.toList());
            testResultRequestDTO.setRegions(regionCodes);
        }

        // 2. educationLevels
        List<UserEducationLevelVO> educationLevels = userPolicyCondition.getEducationLevels();
        if (educationLevels != null && !educationLevels.isEmpty()) {
            List<String> levelNames = educationLevels.stream()
                    .map(level -> policyDataHolder.getEducationLevelName(level.getEducationLevelId()))
                    .collect(Collectors.toList());
            testResultRequestDTO.setEducationLevels(levelNames);
        }

        // 3. employmentStatuses
        List<UserEmploymentStatusVO> employmentStatuses = userPolicyCondition.getEmploymentStatuses();
        if (employmentStatuses != null && !employmentStatuses.isEmpty()) {
            List<String> statusNames = employmentStatuses.stream()
                    .map(status -> policyDataHolder.getEmploymentStatusName(status.getEmploymentStatusId()))
                    .collect(Collectors.toList());
            testResultRequestDTO.setEmploymentStatuses(statusNames);
        }

        // 4. majors
        List<UserMajorVO> majors = userPolicyCondition.getMajors();
        if (majors != null && !majors.isEmpty()) {
            List<String> majorNames = majors.stream()
                    .map(major -> policyDataHolder.getMajorName(major.getMajorId()))
                    .collect(Collectors.toList());
            testResultRequestDTO.setMajors(majorNames);
        }

        // 5. specialConditions
        List<UserSpecialConditionVO> specialConditions = userPolicyCondition.getSpecialConditions();
        if (specialConditions != null && !specialConditions.isEmpty()) {
            List<String> conditionNames = specialConditions.stream()
                    .map(condition -> policyDataHolder.getSpecialConditionName(condition.getSpecialConditionId()))
                    .collect(Collectors.toList());
            testResultRequestDTO.setSpecialConditions(conditionNames);
        }

        // 6. keywords
        List<UserPolicyKeywordVO> keywords = userPolicyCondition.getKeywords();
        if (keywords != null && !keywords.isEmpty()) {
            List<String> keywordNames = keywords.stream()
                    .map(keyword -> policyDataHolder.getKeywordName(keyword.getKeywordId()))
                    .collect(Collectors.toList());
            testResultRequestDTO.setKeywords(keywordNames);
        }

        return testResultRequestDTO;
    }

    /**
     * 사용자 정책 조건을 저장합니다.
     * @param username 사용자 이름
     * @param testResultRequestDTO 사용자 정책 DTO
     * @return 저장된 사용자 정책 DTO
     */
    @Transactional
    @Override
    public TestResultRequestDTO saveUserPolicyCondition(String username, TestResultRequestDTO testResultRequestDTO) {
        MemberVO member = memberMapper.get(username);

        Long userId = member.getUserId();

        UserPolicyConditionVO condition = new UserPolicyConditionVO();
        condition.setUserId(userId);
        condition.setAge(testResultRequestDTO.getAge());
        condition.setMarriage(testResultRequestDTO.getMarriage());
        condition.setIncome(testResultRequestDTO.getIncome());
        condition.setMoneyRank(testResultRequestDTO.getMoneyRank());
        condition.setPeriodRank(testResultRequestDTO.getPeriodRank());
        condition.setPopularityRank(testResultRequestDTO.getPopularityRank());
        userPolicyMapper.saveUserPolicyCondition(condition);
        Long userPolicyConditionId = condition.getId();

        // 1. regions - from List<String> regionCodes
        if (testResultRequestDTO.getRegions() != null && !testResultRequestDTO.getRegions().isEmpty()) {
            List<UserRegionVO> regions = testResultRequestDTO.getRegions().stream()
                    .map(name -> {
                        Long regionId = policyDataHolder.getRegionId(name);
                        if (regionId != null) {
                            UserRegionVO vo = new UserRegionVO();
                            vo.setRegionId(regionId);
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Region name {} not found", name);
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
        if (testResultRequestDTO.getEducationLevels() != null && !testResultRequestDTO.getEducationLevels().isEmpty()) {
            List<UserEducationLevelVO> educationLevels = testResultRequestDTO.getEducationLevels().stream()
                    .map(name -> {
                        Long educationLevelId = policyDataHolder.getEducationLevelId(name);
                        if (educationLevelId != null) {
                            UserEducationLevelVO vo = new UserEducationLevelVO();
                            vo.setEducationLevelId(educationLevelId);
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
        if (testResultRequestDTO.getEmploymentStatuses() != null && !testResultRequestDTO.getEmploymentStatuses().isEmpty()) {
            List<UserEmploymentStatusVO> statuses = testResultRequestDTO.getEmploymentStatuses().stream()
                    .map(name -> {
                        Long employmentStatusId = policyDataHolder.getEmploymentStatusId(name);
                        if (employmentStatusId != null) {
                            UserEmploymentStatusVO vo = new UserEmploymentStatusVO();
                            vo.setEmploymentStatusId(employmentStatusId);
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
        if (testResultRequestDTO.getMajors() != null && !testResultRequestDTO.getMajors().isEmpty()) {
            List<UserMajorVO> majors = testResultRequestDTO.getMajors().stream()
                    .map(name -> {
                        Long majorId = policyDataHolder.getMajorId(name);
                        if (majorId != null) {
                            UserMajorVO vo = new UserMajorVO();
                            vo.setMajorId(majorId);
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
        if (testResultRequestDTO.getSpecialConditions() != null && !testResultRequestDTO.getSpecialConditions().isEmpty()) {
            List<UserSpecialConditionVO> specialConditions = testResultRequestDTO.getSpecialConditions().stream()
                    .map(name -> {
                        Long specialConditionId = policyDataHolder.getSpecialConditionId(name);
                        if (specialConditionId != null) {
                            UserSpecialConditionVO vo = new UserSpecialConditionVO();
                            vo.setSpecialConditionId(specialConditionId);
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
        if (testResultRequestDTO.getKeywords() != null && !testResultRequestDTO.getKeywords().isEmpty()) {
            List<UserPolicyKeywordVO> keywords = testResultRequestDTO.getKeywords().stream()
                    .map(name -> {
                        Long keywordId = policyDataHolder.getKeywordId(name);
                        if (keywordId != null) {
                            UserPolicyKeywordVO vo = new UserPolicyKeywordVO();
                            vo.setKeywordId(keywordId);
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

        // 사용자 벡터 값 계산 및 저장 (중복 방지)
        UserVectorVO userVector = userPolicyMapper.findUserVectorByUserId(userId);
        if (userVector == null) {
            userVector = new UserVectorVO();
            userVector.setUserId(userId);
        }
        userVector.setVecBenefitAmount(getVectorValue(testResultRequestDTO.getMoneyRank()));
        userVector.setVecDeadline(getVectorValue(testResultRequestDTO.getPeriodRank()));
        userVector.setVecViews(getVectorValue(testResultRequestDTO.getPopularityRank()));

        if (userVector.getId() == null) {
            userPolicyMapper.saveUserVector(userVector);
        } else {
            userPolicyMapper.updateUserVector(userVector);
        }

        return testResultRequestDTO;
    }

    /**
     * 사용자 정책 조건을 수정합니다.
     * @param username 사용자 이름
     * @param testResultRequestDTO 수정할 정책 조건 DTO
     * @return 수정된 정책 조건 DTO
     */
    @Transactional
    @Override
    public TestResultRequestDTO updateUserPolicyCondition(String username, TestResultRequestDTO testResultRequestDTO) {
        MemberVO member = memberMapper.get(username);
        Long userId = member.getUserId();

        UserPolicyConditionVO existingCondition = userPolicyMapper.findUserPolicyConditionByUserId(userId);
        if (existingCondition == null) {
            log.error("수정할 사용자 정책 조건을 찾을 수 없습니다: userId={}", userId);
            return null; // Or throw an exception
        }
        Long userPolicyConditionId = existingCondition.getId();

        // 1. Delete existing related data
        userPolicyMapper.deleteUserMajorsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserSpecialConditionsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserPolicyKeywordsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserPolicyRegionsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserEmploymentStatusesByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserEducationLevelsByConditionId(userPolicyConditionId);

        // 2. Update basic condition info
        existingCondition.setAge(testResultRequestDTO.getAge());
        existingCondition.setMarriage(testResultRequestDTO.getMarriage());
        existingCondition.setIncome(testResultRequestDTO.getIncome());
        existingCondition.setMoneyRank(testResultRequestDTO.getMoneyRank());
        existingCondition.setPeriodRank(testResultRequestDTO.getPeriodRank());
        existingCondition.setPopularityRank(testResultRequestDTO.getPopularityRank());
        userPolicyMapper.updateUserPolicyCondition(existingCondition);

        // 3. Insert new data based on DTO (same logic as save)
        if (testResultRequestDTO.getRegions() != null && !testResultRequestDTO.getRegions().isEmpty()) {
            List<UserRegionVO> regions = testResultRequestDTO.getRegions().stream()
                    .map(name -> {
                        Long regionId = policyDataHolder.getRegionId(name);
                        if (regionId != null) {
                            UserRegionVO vo = new UserRegionVO();
                            vo.setRegionId(regionId);
                            vo.setUserPolicyConditionId(userPolicyConditionId);
                            return vo;
                        } else {
                            log.warn("Region name {} not found", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!regions.isEmpty()) {
                userPolicyMapper.saveUserPolicyRegions(regions);
            }
        }

        if (testResultRequestDTO.getEducationLevels() != null && !testResultRequestDTO.getEducationLevels().isEmpty()) {
            List<UserEducationLevelVO> educationLevels = testResultRequestDTO.getEducationLevels().stream()
                    .map(name -> {
                        Long educationLevelId = policyDataHolder.getEducationLevelId(name);
                        if (educationLevelId != null) {
                            UserEducationLevelVO vo = new UserEducationLevelVO();
                            vo.setEducationLevelId(educationLevelId);
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

        if (testResultRequestDTO.getEmploymentStatuses() != null && !testResultRequestDTO.getEmploymentStatuses().isEmpty()) {
            List<UserEmploymentStatusVO> statuses = testResultRequestDTO.getEmploymentStatuses().stream()
                    .map(name -> {
                        Long employmentStatusId = policyDataHolder.getEmploymentStatusId(name);
                        if (employmentStatusId != null) {
                            UserEmploymentStatusVO vo = new UserEmploymentStatusVO();
                            vo.setEmploymentStatusId(employmentStatusId);
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

        if (testResultRequestDTO.getMajors() != null && !testResultRequestDTO.getMajors().isEmpty()) {
            List<UserMajorVO> majors = testResultRequestDTO.getMajors().stream()
                    .map(name -> {
                        Long majorId = policyDataHolder.getMajorId(name);
                        if (majorId != null) {
                            UserMajorVO vo = new UserMajorVO();
                            vo.setMajorId(majorId);
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

        if (testResultRequestDTO.getSpecialConditions() != null && !testResultRequestDTO.getSpecialConditions().isEmpty()) {
            List<UserSpecialConditionVO> specialConditions = testResultRequestDTO.getSpecialConditions().stream()
                    .map(name -> {
                        Long specialConditionId = policyDataHolder.getSpecialConditionId(name);
                        if (specialConditionId != null) {
                            UserSpecialConditionVO vo = new UserSpecialConditionVO();
                            vo.setSpecialConditionId(specialConditionId);
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

        if (testResultRequestDTO.getKeywords() != null && !testResultRequestDTO.getKeywords().isEmpty()) {
            List<UserPolicyKeywordVO> keywords = testResultRequestDTO.getKeywords().stream()
                    .map(name -> {
                        Long keywordId = policyDataHolder.getKeywordId(name);
                        if (keywordId != null) {
                            UserPolicyKeywordVO vo = new UserPolicyKeywordVO();
                            vo.setKeywordId(keywordId);
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

        // 4. Update filtered policy list
        userPolicyMapper.deleteUserFilteredPoliciesByUserId(userId);
        saveUserFilteredPolicies(userId);

        // 사용자 벡터 값 계산 및 수정
        UserVectorVO userVector = userPolicyMapper.findUserVectorByUserId(userId);
        if (userVector == null) {
            userVector = new UserVectorVO();
            userVector.setUserId(userId);
        }
        userVector.setVecBenefitAmount(getVectorValue(testResultRequestDTO.getMoneyRank()));
        userVector.setVecDeadline(getVectorValue(testResultRequestDTO.getPeriodRank()));
        userVector.setVecViews(getVectorValue(testResultRequestDTO.getPopularityRank()));

        if (userVector.getId() == null) {
            userPolicyMapper.saveUserVector(userVector);
        } else {
            userPolicyMapper.updateUserVector(userVector);
        }

        return testResultRequestDTO;
    }

    /**
     * 사용자 정책 조건 및 관련 데이터 전체 삭제
     * @param username 사용자 이름
     */
    @Transactional
    @Override
    public void deleteUserPolicyCondition(String username) {
        MemberVO member = memberMapper.get(username);
        if (member == null) {
            log.error("사용자를 찾을 수 없습니다: username={}", username);
            return;
        }
        Long userId = member.getUserId();
        UserPolicyConditionVO existingCondition = userPolicyMapper.findUserPolicyConditionByUserId(userId);
        if (existingCondition == null) {
            log.info("삭제할 사용자 정책 조건이 없습니다: userId={}", userId);
            return;
        }
        Long userPolicyConditionId = existingCondition.getId();

        // 1. 자식 테이블(연관 테이블) 먼저 삭제
        userPolicyMapper.deleteUserMajorsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserSpecialConditionsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserPolicyKeywordsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserPolicyRegionsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserEmploymentStatusesByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserEducationLevelsByConditionId(userPolicyConditionId);
        userPolicyMapper.deleteUserFilteredPoliciesByUserId(userId);

        // 2. 벡터 테이블 삭제
        userPolicyMapper.deleteUserVectorByUserId(userId);

        // 3. 정책 조건 테이블 삭제
        userPolicyMapper.deleteUserPolicyConditionById(userPolicyConditionId);

        log.info("사용자 정책 조건 및 관련 데이터 삭제 완료: userId={}", userId);
    }

    /**
     * 사용자 정책 조건에 맞는 정책을 필터링하여 저장합니다.
     * @param userId 사용자 ID
     */
    public void saveUserFilteredPolicies(Long userId) {

        UserPolicyConditionVO userPolicyCondition = userPolicyMapper.findUserPolicyConditionByUserId(userId);
        if (userPolicyCondition == null) {
            log.info("사용자 정책 조건이 존재하지 않습니다: userId={}", userId);
            return;
        }

        // 사용자 정책 조건을 기반으로 검색 요청 DTO 생성
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setAge(userPolicyCondition.getAge());
        searchRequestDTO.setMarriage(userPolicyCondition.getMarriage());
        searchRequestDTO.setIncome(userPolicyCondition.getIncome());
        searchRequestDTO.setRegions(userPolicyCondition.getRegions().stream()
                .map(region -> policyDataHolder.getRegionName(region.getRegionId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setEducationLevels(userPolicyCondition.getEducationLevels().stream()
                .map(level -> policyDataHolder.getEducationLevelName(level.getEducationLevelId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setEmploymentStatuses(userPolicyCondition.getEmploymentStatuses().stream()
                .map(status -> policyDataHolder.getEmploymentStatusName(status.getEmploymentStatusId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setMajors(userPolicyCondition.getMajors().stream()
                .map(major -> policyDataHolder.getMajorName(major.getMajorId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setSpecialConditions(userPolicyCondition.getSpecialConditions().stream()
                .map(condition -> policyDataHolder.getSpecialConditionName(condition.getSpecialConditionId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setKeywords(userPolicyCondition.getKeywords().stream()
                .map(keyword -> policyDataHolder.getKeywordName(keyword.getKeywordId()))
                .collect(Collectors.toList()));

        // 빈 문자열을 제거하는 유틸 메서드
        searchRequestDTO.setRegions(removeEmptyStrings(searchRequestDTO.getRegions()));
        searchRequestDTO.setEducationLevels(removeEmptyStrings(searchRequestDTO.getEducationLevels()));
        searchRequestDTO.setEmploymentStatuses(removeEmptyStrings(searchRequestDTO.getEmploymentStatuses()));
        searchRequestDTO.setMajors(removeEmptyStrings(searchRequestDTO.getMajors()));
        searchRequestDTO.setSpecialConditions(removeEmptyStrings(searchRequestDTO.getSpecialConditions()));
        searchRequestDTO.setKeywords(removeEmptyStrings(searchRequestDTO.getKeywords()));

        // 지역 코드 확장 로직 추가
        List<String> originalRegions = searchRequestDTO.getRegions();
        Set<String> extendedRegions = new HashSet<>(originalRegions);
        for (String region : originalRegions) {
            if (region.length() >= 2) {
                String generalizedRegion = region.substring(0, 2) + "000";
                extendedRegions.add(generalizedRegion);
            }
        }
        searchRequestDTO.setRegions(new ArrayList<>(extendedRegions));

        List<PolicyWithVectorDTO> policiesWithVectors = userPolicyMapper.findFilteredPoliciesWithVectors(searchRequestDTO);
        // 마감일 필터링 로직 추가 (로그 포함)
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatterDot = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter formatterPlain = DateTimeFormatter.ofPattern("yyyyMMdd");

        List<Long> matchingPolicyIds = policiesWithVectors.stream()
            .filter(policy -> {
                String endDateStr = policy.getEndDate();
                if (endDateStr == null || !endDateStr.contains("~")) {
                    log.debug("[마감일필터] 정책ID={} endDate='{}' → 마감일 ��보 없음, 포함", policy.getPolicyId(), endDateStr);
                    return true;
                }
                String[] parts = endDateStr.split("~");
                if (parts.length < 2) {
                    log.debug("[마감일필터] 정책ID={} endDate='{}' → ~ 이후 정보 없음, 포함", policy.getPolicyId(), endDateStr);
                    return true;
                }
                String rawEndDate = parts[1].trim();
                String datePart = rawEndDate.split(" ")[0].trim();
                LocalDate endDate = null;
                try {
                    if (datePart.contains(".")) {
                        endDate = LocalDate.parse(datePart, formatterDot);
                    } else {
                        endDate = LocalDate.parse(datePart, formatterPlain);
                    }
                    boolean include = !endDate.isBefore(today);
                    log.debug("[마감일필터] 정책ID={} endDate='{}' → 마감일={}, 오늘={}, 포함여부={}", policy.getPolicyId(), endDateStr, endDate, today, include);
                    return include;
                } catch (DateTimeParseException e) {
                    log.warn("[마감일필터] 정책ID={} endDate='{}' → 날짜 파싱 실패, 포함", policy.getPolicyId(), endDateStr);
                    return true;
                }
            })
            .map(PolicyWithVectorDTO::getPolicyId)
            .collect(Collectors.toList());

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

    /**
     * 사용자 정책 조건에 맞는 정책을 조회합니다.
     *
     * @param username 사용자 이름
     * @return 정책 목록
     */
    @Override
    public List<SearchResultDTO> searchMatchingPolicy(String username) {

        MemberVO member = memberMapper.get(username);
        if (member == null) {
            log.error("사��자를 찾을 수 없습니다: username={}", username);
            return null; // Or throw an exception
        }
        Long userId = member.getUserId();
        UserPolicyConditionVO userPolicyCondition = userPolicyMapper.findUserPolicyConditionByUserId(userId);
        if (userPolicyCondition == null) {
            log.info("사용자 정책 조건이 존재하지 않습니다: userId={}", userId);
            return null; // Or throw an exception
        }
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setAge(userPolicyCondition.getAge());
        searchRequestDTO.setMarriage(userPolicyCondition.getMarriage());
        searchRequestDTO.setIncome(userPolicyCondition.getIncome());
        searchRequestDTO.setRegions(userPolicyCondition.getRegions().stream()
                .map(region -> policyDataHolder.getRegionName(region.getRegionId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setEducationLevels(userPolicyCondition.getEducationLevels().stream()
                .map(level -> policyDataHolder.getEducationLevelName(level.getEducationLevelId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setEmploymentStatuses(userPolicyCondition.getEmploymentStatuses().stream()
                .map(status -> policyDataHolder.getEmploymentStatusName(status.getEmploymentStatusId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setMajors(userPolicyCondition.getMajors().stream()
                .map(major -> policyDataHolder.getMajorName(major.getMajorId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setSpecialConditions(userPolicyCondition.getSpecialConditions().stream()
                .map(condition -> policyDataHolder.getSpecialConditionName(condition.getSpecialConditionId()))
                .collect(Collectors.toList()));
        searchRequestDTO.setKeywords(userPolicyCondition.getKeywords().stream()
                .map(keyword -> policyDataHolder.getKeywordName(keyword.getKeywordId()))
                .collect(Collectors.toList()));

        // 빈 문자열을 제거하는 유틸 메서드
        searchRequestDTO.setRegions(removeEmptyStrings(searchRequestDTO.getRegions()));
        searchRequestDTO.setEducationLevels(removeEmptyStrings(searchRequestDTO.getEducationLevels()));
        searchRequestDTO.setEmploymentStatuses(removeEmptyStrings(searchRequestDTO.getEmploymentStatuses()));
        searchRequestDTO.setMajors(removeEmptyStrings(searchRequestDTO.getMajors()));
        searchRequestDTO.setSpecialConditions(removeEmptyStrings(searchRequestDTO.getSpecialConditions()));
        searchRequestDTO.setKeywords(removeEmptyStrings(searchRequestDTO.getKeywords()));

        // 지역 코드 확장 로직 추가
        List<String> originalRegions = searchRequestDTO.getRegions();
        Set<String> extendedRegions = new HashSet<>(originalRegions);
        for (String region : originalRegions) {
            if (region.length() >= 2) {
                String generalizedRegion = region.substring(0, 2) + "000";
                extendedRegions.add(generalizedRegion);
            }
        }
        searchRequestDTO.setRegions(new ArrayList<>(extendedRegions));


        // 1. 벡터 정보를 포함한 정책 목록 조회 (N+1 문제 해결)
        List<PolicyWithVectorDTO> policiesWithVectors = userPolicyMapper.findFilteredPoliciesWithVectors(searchRequestDTO);
        // 마감일 필터링 로직 추가 (로그 포함)
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatterDot = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter formatterPlain = DateTimeFormatter.ofPattern("yyyyMMdd");
        List<PolicyWithVectorDTO> filteredPoliciesWithVectors = policiesWithVectors.stream()
            .filter(policy -> {
                String endDateStr = policy.getEndDate();
                if (endDateStr == null || !endDateStr.contains("~")) {
                    log.debug("[마감일필터] 정책ID={} endDate='{}' → 마감일 정보 없음, 포함", policy.getPolicyId(), endDateStr);
                    return true;
                }
                String[] parts = endDateStr.split("~");
                if (parts.length < 2) {
                    log.debug("[마감일필터] 정책ID={} endDate='{}' → ~ 이후 정보 없음, 포함", policy.getPolicyId(), endDateStr);
                    return true;
                }
                String rawEndDate = parts[1].trim();
                String datePart = rawEndDate.split(" ")[0].trim();
                LocalDate endDate = null;
                try {
                    if (datePart.contains(".")) {
                        endDate = LocalDate.parse(datePart, formatterDot);
                    } else {
                        endDate = LocalDate.parse(datePart, formatterPlain);
                    }
                    boolean include = !endDate.isBefore(today);
                    log.debug("[마감일필터] 정책ID={} endDate='{}' → 마감일={}, 오늘={}, 포함여부={}", policy.getPolicyId(), endDateStr, endDate, today, include);
                    return include;
                } catch (DateTimeParseException e) {
                    log.warn("[마감일필터] 정책ID={} endDate='{}' → 날짜 파싱 실패, 포함", policy.getPolicyId(), endDateStr);
                    return true;
                }
            })
            .collect(Collectors.toList());

        // 2. 사용자 벡터 조회
        UserVectorVO userVector = userPolicyMapper.findUserVectorByUserId(userId);

        List<SearchResultDTO> searchResultDTO;

        if (userVector == null) {
            // 사용자 벡터가 없으면 기본 정렬 (벡터 없이 반환)
            log.info("사용자 벡터 없음 - 기본 정렬 적용, userId: {}", userId);
            searchResultDTO = filteredPoliciesWithVectors.stream()
                    .map(VectorUtil::toSearchResultDTO)
                    .collect(Collectors.toList());
        } else {
            // 3. 코사인 유사도 계산 및 정렬
            log.info("벡터 기반 추천 시작 - userId: {}, 정책 수: {}", userId, filteredPoliciesWithVectors.size());

            searchResultDTO = filteredPoliciesWithVectors.stream()
                    .filter(policy -> policy.getVecBenefitAmount() != null)
                    .map(policy -> {
                        // 코사인 유사도 계산
                        double similarity = VectorUtil.calculateCosineSimilarity(userVector, policy);
                        policy.setSimilarity(similarity);
                        return policy;
                    })
                    .sorted((p1, p2) -> Double.compare(p2.getSimilarity(), p1.getSimilarity()))
                    .map(VectorUtil::toSearchResultDTO)
                    .collect(Collectors.toList());

            log.info("벡터 기반 추천 완료 - 추천 정책 수: {}", searchResultDTO.size());
        }

        return searchResultDTO;
    }

    /**
     * 사용자 선택한 조건과 작성한 검색어에 따라 필터링된 정책 목록을 조회합니다.
     *
     * @param username 사용자 이름
     * @param searchRequestDTO 검색 요청 DTO
     * @return 필터링된 정책 목록
     */
    @Override
    public List<SearchResultDTO> searchFilteredPolicy(String username, SearchRequestDTO searchRequestDTO){

        MemberVO member = memberMapper.get(username);

        if (member == null) {
            log.error("사용자를 찾을 수 없습니다: username={}", username);
            return null; // Or throw an exception
        }


        Long userId = member.getUserId();

        // 빈 문자열을 제거하는 유틸 메서드
        searchRequestDTO.setRegions(removeEmptyStrings(searchRequestDTO.getRegions()));
        searchRequestDTO.setEducationLevels(removeEmptyStrings(searchRequestDTO.getEducationLevels()));
        searchRequestDTO.setEmploymentStatuses(removeEmptyStrings(searchRequestDTO.getEmploymentStatuses()));
        searchRequestDTO.setMajors(removeEmptyStrings(searchRequestDTO.getMajors()));
        searchRequestDTO.setSpecialConditions(removeEmptyStrings(searchRequestDTO.getSpecialConditions()));
        searchRequestDTO.setKeywords(removeEmptyStrings(searchRequestDTO.getKeywords()));

        // 지역 코드 확장 로직 추가
        List<String> originalRegions = searchRequestDTO.getRegions();
        Set<String> extendedRegions = new HashSet<>(originalRegions);

        for (String region : originalRegions) {
            if (region.length() >= 2) {
                String generalizedRegion = region.substring(0, 2) + "000";
                extendedRegions.add(generalizedRegion);
            }
        }

        searchRequestDTO.setRegions(new ArrayList<>(extendedRegions));

        // 1. 벡터 정보를 포함한 정책 목록 조회 (N+1 문제 해결)
        List<PolicyWithVectorDTO> policiesWithVectors = userPolicyMapper.findFilteredPoliciesWithVectors(searchRequestDTO);

        // 2. 사용자 벡터 조회
        UserVectorVO userVector = userPolicyMapper.findUserVectorByUserId(userId);

        List<SearchResultDTO> searchResultDTO;

        if (userVector == null) {
            // 사용자 벡터가 없으면 기본 정렬 (벡터 없이 반환)
            log.info("사용자 벡터 없음 - 기본 정렬 적용, userId: {}", userId);
            searchResultDTO = policiesWithVectors.stream()
                    .map(VectorUtil::toSearchResultDTO)
                    .collect(Collectors.toList());
        } else {
            // 3. 코사인 유사도 계산 및 정렬
            log.info("벡터 기반 추천 시작 - userId: {}, 정책 수: {}", userId, policiesWithVectors.size());

            searchResultDTO = policiesWithVectors.stream()
                    .filter(policy -> policy.getVecBenefitAmount() != null) // 벡터가 있는 정책만
                    .map(policy -> {
                        // 코사인 유사도 계산
                        double similarity = VectorUtil.calculateCosineSimilarity(userVector, policy);
                        policy.setSimilarity(similarity);
                        return policy;
                    })
                    .sorted((p1, p2) -> Double.compare(p2.getSimilarity(), p1.getSimilarity())) // 유사도 내림차순
                    .map(VectorUtil::toSearchResultDTO) // SearchResultDTO로 변환
                    .collect(Collectors.toList());

            log.info("벡터 기반 추천 완료 - 추천 정책 수: {}", searchResultDTO.size());
        }

        return searchResultDTO;
    }

    /**
     * 주어진 리스트에서 빈 문자열을 제거하는 유틸리티 메소드.
     * @param list 문자열 리스트
     * @return 빈 문자열��� 제거된 리스트
     */
    private List<String> removeEmptyStrings(List<String> list) {
        if (list == null) return null;
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .toList();
    }

    private BigDecimal getVectorValue(int rank) {
        return switch (rank) {
            case 1 -> new BigDecimal("0.6");
            case 2 -> new BigDecimal("0.5");
            case 3 -> new BigDecimal("0.4");
            default -> new BigDecimal("0.0");
        };
    }

    /**
     * 검색어를 Redis의 Sorted Set에 저장하고 점수를 1 증가시킵니다.
     * @param searchText 검색어
     */
    public void saveSearchText(String searchText) {
        if (searchText != null && !searchText.trim().isEmpty()) {
            try {
                ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
                zSetOperations.incrementScore(POPULAR_KEYWORDS_KEY, searchText, 1);
                log.info("인기 검색어 저장: '{}'", searchText);
            } catch (Exception e) {
                log.error("인기 검색어 저장 실패: {}", searchText, e);
            }
        }
    }


    /**
     * 인기 검색어 목록을 조회합니다.
     * @param count 조회할 개수
     * @return 인기 검색어 목록
     */
    public List<String> getPopularKeywords(int count) {
        try {
            ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
            Set<String> keywords = zSetOperations.reverseRange(POPULAR_KEYWORDS_KEY, 0, count - 1);
            return new ArrayList<>(keywords != null ? keywords : Collections.emptyList());
        } catch (Exception e) {
            log.error("인기 검색어 조회 실패", e);
            return Collections.emptyList();
        }
    }

    /**
     * 최근 검색어를 저장합니다.
     * @param username 사용자 이름
     * @param searchText 검색어
     */
    @Override
    public void saveRecentSearch(String username, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return;
        }
        String key = RECENT_KEYWORDS_KEY_PREFIX + username;

        try {
            ListOperations<String, String> listOperations = redisTemplate.opsForList();
            // 기존에 있던 검색어는 삭제
            listOperations.remove(key, 0, searchText);
            // 새로운 검색어를 맨 앞에 추가
            listOperations.leftPush(key, searchText);
            // 리스트의 길이를 6으로 제한
            listOperations.trim(key, 0, MAX_RECENT_KEYWORDS - 1);
            log.info("최근 검색어 저장: username={}, keyword={}", username, searchText);
        } catch (Exception e) {
            log.error("최근 검색어 저장 실패: username={}, keyword={}", username, searchText, e);
        }
    }

    /**
     * 최근 검색어 목록을 조회합니다.
     * @param username 사용자 이름
     * @return 최근 검색어 목록
     */
    @Override
    public List<String> getRecentSearches(String username) {
        String key = RECENT_KEYWORDS_KEY_PREFIX + username;
        try {
            ListOperations<String, String> listOperations = redisTemplate.opsForList();
            List<String> recentKeywords = listOperations.range(key, 0, MAX_RECENT_KEYWORDS - 1);
            return recentKeywords != null ? recentKeywords : Collections.emptyList();
        } catch (Exception e) {
            log.error("최근 검색어 조회 실패: username={}", username, e);
            return Collections.emptyList();
        }
    }
}
