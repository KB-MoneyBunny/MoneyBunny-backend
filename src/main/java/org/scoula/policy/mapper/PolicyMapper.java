package org.scoula.policy.mapper;

import org.apache.ibatis.annotations.Param;
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
import org.scoula.policy.dto.PolicyDetailDTO;

public interface PolicyMapper {

    /** 전체 정책 수 확인 */
    int countAllPolicies();

    /** 정책 존재 여부 확인 */
    boolean existsByPolicyNo(@Param("policyNo") String policyNo);

    /** 정책 저장 */
    void insertPolicy(YouthPolicyVO policyVO);

    /** 정책 조건 저장 */
    void insertCondition(YouthPolicyConditionVO conditionVO);

    /** 정책 운영 기간 저장 */
    void insertPeriod(YouthPolicyPeriodVO periodVO);

    // ────────────────────────────────────────
    // 📌 키워드 관련
    // ────────────────────────────────────────

    /** 키워드 조회 (중복 확인) */
    PolicyKeywordVO findKeywordByName(@Param("keyword") String keyword);

    /** 키워드 마스터 저장 */
    void insertPolicyKeyword(PolicyKeywordVO keywordVO);

    /** 정책-키워드 연결 저장 */
    void insertYouthPolicyKeyword(YouthPolicyKeywordVO keywordVO);


    // ────────────────────────────────────────
    // 📌 지역 관련
    // ────────────────────────────────────────

    /** 지역 조회 (중복 확인) */
    PolicyRegionVO findRegionByCode(@Param("regionCode") String regionCode);

    /** 지역 마스터 저장 */
    void insertPolicyRegion(PolicyRegionVO regionVO);

    /** 정책-지역 연결 저장 */
    void insertYouthPolicyRegion(YouthPolicyRegionVO regionVO);
    // ────────────────────────────────────────
    // 📌 전공(Major) 관련
    // ────────────────────────────────────────

    /** 전공 조회 */
    PolicyMajorVO findMajorByName(@Param("major") String major);

    /** 전공 마스터 저장 */
    void insertPolicyMajor(PolicyMajorVO vo);

    /** 정책-전공 연결 저장 */
    void insertYouthPolicyMajor(YouthPolicyMajorVO vo);

    // ────────────────────────────────────────
    // 📌 학력(Education Level) 관련
    // ────────────────────────────────────────

    /** 학력 조회 */
    PolicyEducationLevelVO findEducationLevelByName(@Param("educationLevel") String educationLevel);

    /** 학력 마스터 저장 */
    void insertPolicyEducationLevel(PolicyEducationLevelVO vo);

    /** 정책-학력 연결 저장 */
    void insertYouthPolicyEducationLevel(YouthPolicyEducationLevelVO vo);

    // ────────────────────────────────────────
    // 📌 취업 상태(Employment Status) 관련
    // ────────────────────────────────────────

    /** 취업 상태 조회 */
    PolicyEmploymentStatusVO findEmploymentStatusByName(@Param("employmentStatus") String employmentStatus);

    /** 취업 상태 마스터 저장 */
    void insertPolicyEmploymentStatus(PolicyEmploymentStatusVO vo);

    /** 정책-취업 상태 연결 저장 */
    void insertYouthPolicyEmploymentStatus(YouthPolicyEmploymentStatusVO vo);

    // ────────────────────────────────────────
    // 📌 특수 조건(Special Condition) 관련
    // ────────────────────────────────────────

    /** 특수 조건 조회 */
    PolicySpecialConditionVO findSpecialConditionByName(@Param("specialCondition") String specialCondition);

    /** 특수 조건 마스터 저장 */
    void insertPolicySpecialCondition(PolicySpecialConditionVO vo);

    /** 정책-특수 조건 연결 저장 */
    void insertYouthPolicySpecialCondition(YouthPolicySpecialConditionVO vo);

    YouthPolicyPeriodVO findYouthPolicyPeriodByPolicyId(Long policyId);

    YouthPolicyVO findYouthPolicyById(Long policyId);

    PolicyDetailDTO findPolicyDetailById(Long policyId);
}
