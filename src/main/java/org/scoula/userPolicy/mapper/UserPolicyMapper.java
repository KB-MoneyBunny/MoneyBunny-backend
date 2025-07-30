package org.scoula.userPolicy.mapper;

import org.scoula.userPolicy.domain.*;
import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;

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

    List<SearchResultDTO> findFilteredPolicies(SearchRequestDTO searchRequestDTO);

    // 필터링된 정책 목록을 저장
    void saveUserFilteredPolicies(List<UserFilteredPoliciesVO> filteredPolicies);

    // 사용자 정책 조건을 수정
    void updateUserPolicyCondition(UserPolicyConditionVO userPolicyCondition);

    // 사용자 정책 조건 관련 데이터를 삭제
    void deleteUserMajorsByConditionId(Long userPolicyConditionId);
    void deleteUserSpecialConditionsByConditionId(Long userPolicyConditionId);
    void deleteUserPolicyKeywordsByConditionId(Long userPolicyConditionId);
    void deleteUserPolicyRegionsByConditionId(Long userPolicyConditionId);
    void deleteUserEmploymentStatusesByConditionId(Long userPolicyConditionId);
    void deleteUserEducationLevelsByConditionId(Long userPolicyConditionId);
    void deleteUserFilteredPoliciesByUserId(Long userId);

    // 사용자 벡터를 저장
    void saveUserVector(UserVectorVO userVector);

    // 사용자 벡터를 수정
    void updateUserVector(UserVectorVO userVector);

    // 사용자 ID로 사용자 벡터를 조회
    UserVectorVO findUserVectorByUserId(Long userId);
}
