package org.scoula.userPolicy.mapper;

import org.scoula.userPolicy.domain.*;

import java.math.BigDecimal;
import java.util.List;

public interface UserPolicyMapper {

    // 사용자 정책 조건을 저장
    Long saveUserPolicyCondition(UserPolicyConditionVO userPolicyCondition);
    void saveUserMajors(List<UserMajorVO> majors);
    void saveUserSpecialConditions(List<UserSpecialConditionVO> specialConditions);
    void saveUserPolicyKeywords(List<UserPolicyKeywordVO> keywords);
    void saveUserPolicyRegions(List<UserRegionVO> regions);
    void saveUserEmploymentStatuses(List<UserEmploymentStatusVO> employmentStatuses);
    void saveUserEducationLevels(List<UserEducationLevelVO> educationLevels);

    // 사용자 정책 조건을 조회
    UserPolicyConditionVO findUserPolicyConditionByUserId(Long userId);

    // 사용자 정책 조건에 맞는 정책 ID 목록을 조회
    List<Long> findMatchingPolicyIds(UserPolicyConditionVO userPolicyCondition);

    // 사용자 정책 점수를 저장
    BigDecimal saveUserPolicyScore(UserPolicyScoreVO userPolicyScore);
}
