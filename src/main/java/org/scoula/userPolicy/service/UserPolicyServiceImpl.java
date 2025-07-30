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
import org.scoula.policy.util.PolicyDataHolder;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.userPolicy.domain.*;
import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPolicyServiceImpl implements UserPolicyService {

    private final UserPolicyMapper userPolicyMapper;
    private final PolicyMapper policyMapper;
    private final MemberMapper memberMapper;
    private final PolicyDataHolder policyDataHolder;

    @Transactional
    @Override
    public UserPolicyDTO getUserPolicyCondition(String username) {


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

        UserPolicyDTO userPolicyDTO = new UserPolicyDTO();
        userPolicyDTO.setAge(userPolicyCondition.getAge());
        userPolicyDTO.setMarriage(userPolicyCondition.getMarriage());
        userPolicyDTO.setIncome(userPolicyCondition.getIncome());

        // 1. regions
        List<UserRegionVO> regions = userPolicyCondition.getRegions();
        if (regions != null && !regions.isEmpty()) {
            List<String> regionCodes = regions.stream()
                    .map(region -> policyDataHolder.getRegionName(region.getRegionId()))
                    .collect(Collectors.toList());
            userPolicyDTO.setRegions(regionCodes);
        }

        // 2. educationLevels
        List<UserEducationLevelVO> educationLevels = userPolicyCondition.getEducationLevels();
        if (educationLevels != null && !educationLevels.isEmpty()) {
            List<String> levelNames = educationLevels.stream()
                    .map(level -> policyDataHolder.getEducationLevelName(level.getEducationLevelId()))
                    .collect(Collectors.toList());
            userPolicyDTO.setEducationLevels(levelNames);
        }

        // 3. employmentStatuses
        List<UserEmploymentStatusVO> employmentStatuses = userPolicyCondition.getEmploymentStatuses();
        if (employmentStatuses != null && !employmentStatuses.isEmpty()) {
            List<String> statusNames = employmentStatuses.stream()
                    .map(status -> policyDataHolder.getEmploymentStatusName(status.getEmploymentStatusId()))
                    .collect(Collectors.toList());
            userPolicyDTO.setEmploymentStatuses(statusNames);
        }

        // 4. majors
        List<UserMajorVO> majors = userPolicyCondition.getMajors();
        if (majors != null && !majors.isEmpty()) {
            List<String> majorNames = majors.stream()
                    .map(major -> policyDataHolder.getMajorName(major.getMajorId()))
                    .collect(Collectors.toList());
            userPolicyDTO.setMajors(majorNames);
        }

        // 5. specialConditions
        List<UserSpecialConditionVO> specialConditions = userPolicyCondition.getSpecialConditions();
        if (specialConditions != null && !specialConditions.isEmpty()) {
            List<String> conditionNames = specialConditions.stream()
                    .map(condition -> policyDataHolder.getSpecialConditionName(condition.getSpecialConditionId()))
                    .collect(Collectors.toList());
            userPolicyDTO.setSpecialConditions(conditionNames);
        }

        // 6. keywords
        List<UserPolicyKeywordVO> keywords = userPolicyCondition.getKeywords();
        if (keywords != null && !keywords.isEmpty()) {
            List<String> keywordNames = keywords.stream()
                    .map(keyword -> policyDataHolder.getKeywordName(keyword.getKeywordId()))
                    .collect(Collectors.toList());
            userPolicyDTO.setKeywords(keywordNames);
        }

        return userPolicyDTO;
    }

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
        if (userPolicyDTO.getEducationLevels() != null && !userPolicyDTO.getEducationLevels().isEmpty()) {
            List<UserEducationLevelVO> educationLevels = userPolicyDTO.getEducationLevels().stream()
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
        if (userPolicyDTO.getEmploymentStatuses() != null && !userPolicyDTO.getEmploymentStatuses().isEmpty()) {
            List<UserEmploymentStatusVO> statuses = userPolicyDTO.getEmploymentStatuses().stream()
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
        if (userPolicyDTO.getMajors() != null && !userPolicyDTO.getMajors().isEmpty()) {
            List<UserMajorVO> majors = userPolicyDTO.getMajors().stream()
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
        if (userPolicyDTO.getSpecialConditions() != null && !userPolicyDTO.getSpecialConditions().isEmpty()) {
            List<UserSpecialConditionVO> specialConditions = userPolicyDTO.getSpecialConditions().stream()
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
        if (userPolicyDTO.getKeywords() != null && !userPolicyDTO.getKeywords().isEmpty()) {
            List<UserPolicyKeywordVO> keywords = userPolicyDTO.getKeywords().stream()
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
        return userPolicyDTO;
    }

    @Transactional
    @Override
    public UserPolicyDTO updateUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO) {
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
        existingCondition.setAge(userPolicyDTO.getAge());
        existingCondition.setMarriage(userPolicyDTO.getMarriage());
        existingCondition.setIncome(userPolicyDTO.getIncome());
        userPolicyMapper.updateUserPolicyCondition(existingCondition);

        // 3. Insert new data based on DTO (same logic as save)
        if (userPolicyDTO.getRegions() != null && !userPolicyDTO.getRegions().isEmpty()) {
            List<UserRegionVO> regions = userPolicyDTO.getRegions().stream()
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

        if (userPolicyDTO.getEducationLevels() != null && !userPolicyDTO.getEducationLevels().isEmpty()) {
            List<UserEducationLevelVO> educationLevels = userPolicyDTO.getEducationLevels().stream()
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

        if (userPolicyDTO.getEmploymentStatuses() != null && !userPolicyDTO.getEmploymentStatuses().isEmpty()) {
            List<UserEmploymentStatusVO> statuses = userPolicyDTO.getEmploymentStatuses().stream()
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

        if (userPolicyDTO.getMajors() != null && !userPolicyDTO.getMajors().isEmpty()) {
            List<UserMajorVO> majors = userPolicyDTO.getMajors().stream()
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

        if (userPolicyDTO.getSpecialConditions() != null && !userPolicyDTO.getSpecialConditions().isEmpty()) {
            List<UserSpecialConditionVO> specialConditions = userPolicyDTO.getSpecialConditions().stream()
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

        if (userPolicyDTO.getKeywords() != null && !userPolicyDTO.getKeywords().isEmpty()) {
            List<UserPolicyKeywordVO> keywords = userPolicyDTO.getKeywords().stream()
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

        return userPolicyDTO;
    }

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

        List<SearchResultDTO> searchResultDTO = userPolicyMapper.findFilteredPolicies(searchRequestDTO);

        // 사용자 정책 조건에 맞는 정책 ID 목록 조회
        List<Long> matchingPolicyIds = searchResultDTO.stream().map(SearchResultDTO::getPolicyId).collect(Collectors.toList());

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
    @Override
    public List<SearchResultDTO> searchMatchingPolicy(String username) {

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
        List<SearchResultDTO> searchResultDTO = userPolicyMapper.findFilteredPolicies(searchRequestDTO);

        return searchResultDTO;
    }

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
        List<SearchResultDTO> searchResultDTO = userPolicyMapper.findFilteredPolicies(searchRequestDTO);


        // 신청 기간에서 마감일 추출
        for(SearchResultDTO resultDTO : searchResultDTO){
            if(resultDTO.getEndDate() != null && resultDTO.getEndDate() != ""){
                String[] Date=resultDTO.getEndDate().split("~");
                if(Date.length==2){
                    resultDTO.setEndDate(Date[1].trim());
                }
            }
        }
        return searchResultDTO;
    }

    // 빈 문자열 제거용 메서드
    private List<String> removeEmptyStrings(List<String> list) {
        if (list == null) return null;
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .toList();
    }





    // 정책 점수 계산에 필요한 상수 정의(추가 논의 필요)
    private static final BigDecimal MAX_AMOUNT_THRESHOLD = new BigDecimal("1000000"); // 혜택 금액 최대치: 백만 원
    private static final BigDecimal MAX_VIEW_THRESHOLD = new BigDecimal("1000"); // 조회수 최대치: 1000 회
    private static final long SCORE_RANGE_DAYS = 100L; // 마감일 기준 최대 범위: 100일

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
