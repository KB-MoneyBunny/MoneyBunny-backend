package org.scoula.userPolicy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.member.mapper.MemberMapper;
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
    public UserPolicyDTO saveUserPolicy(String username, UserPolicyDTO userPolicyDTO) {
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

    public List<String> returnUserPolicyIdList(String username) {
        MemberVO member = memberMapper.get(username);

        Long userId = member.getUserId();
        // 사용자 조건 불러오기
        UserPolicyConditionVO userPolicyCondition = userPolicyMapper.findUserPolicyConditionByUserId(userId);

        return userPolicyMapper.findMatchingPolicyIds(userPolicyCondition);
    }
}
