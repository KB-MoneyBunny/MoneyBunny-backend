package org.scoula.userPolicy.mapper;

import org.scoula.userPolicy.domain.*;

import java.util.List;

public interface UserPolicyMapper {
    Long saveUserPolicyCondition(UserPolicyConditionVO userPolicyCondition);
    void saveUserMajors(List<UserMajorVO> majors);
    void saveUserSpecialConditions(List<UserSpecialConditionVO> specialConditions);
    void saveUserPolicyKeywords(List<UserPolicyKeywordVO> keywords);
    void saveUserPolicyRegions(List<UserRegionVO> regions);
    void saveUserEmploymentStatuses(List<UserEmploymentStatusVO> employmentStatuses);
    void saveUserEducationLevels(List<UserEducationLevelVO> educationLevels);

    UserPolicyConditionVO findUserPolicyConditionByUserId(Long userId);
}
