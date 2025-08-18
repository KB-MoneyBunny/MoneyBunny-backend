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
import org.scoula.policy.domain.master.*;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.region.YouthPolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;
import org.scoula.policy.domain.specialcondition.YouthPolicySpecialConditionVO;
import org.scoula.policy.dto.PolicyDetailDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
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

    /** 기존 정책 업데이트 (조회수, 대분류, 중분류, 신청URL) */
    void updatePolicyInfo(@Param("policyNo") String policyNo, 
                         @Param("views") Long views,
                         @Param("largeCategory") String largeCategory,
                         @Param("mediumCategory") String mediumCategory,
                         @Param("applyUrl") String applyUrl);

    /** 정책 기간 업데이트 */
    void updatePolicyPeriod(@Param("policyNo") String policyNo, 
                           @Param("applyPeriod") String applyPeriod);

    /** 정책 신청URL 업데이트 */
    void updatePolicyApplyUrl(@Param("policyNo") String policyNo, 
                             @Param("applyUrl") String applyUrl);

    // 키워드 관련

    /** 키워드 조회 (중복 확인) */
    PolicyKeywordVO findKeywordByName(@Param("keyword") String keyword);

    /** 전체 키워드 조회 */
    List<PolicyKeywordVO> findAllKeywords();

    /** 키워드 마스터 저장 */
    void insertPolicyKeyword(PolicyKeywordVO keywordVO);

    /** 정책-키워드 연결 저장 */
    void insertYouthPolicyKeyword(YouthPolicyKeywordVO keywordVO);


    // 지역 관련

    /** 지역 조회 (중복 확인) */
    PolicyRegionVO findRegionByCode(@Param("regionCode") String regionCode);

    /** 전체 지역 조회 */
    List<PolicyRegionVO> findAllRegions();

    /** 지역 마스터 저장 */
    void insertPolicyRegion(PolicyRegionVO regionVO);

    /** 정책-지역 연결 저장 */
    void insertYouthPolicyRegion(YouthPolicyRegionVO regionVO);
    // 전공 관련

    /** 전공 조회 */
    PolicyMajorVO findMajorByName(@Param("major") String major);

    /** 전체 전공 조회 */
    List<PolicyMajorVO> findAllMajors();

    /** 전공 마스터 저장 */
    void insertPolicyMajor(PolicyMajorVO vo);

    /** 정책-전공 연결 저장 */
    void insertYouthPolicyMajor(YouthPolicyMajorVO vo);

    // 학력 관련

    /** 학력 조회 */
    PolicyEducationLevelVO findEducationLevelByName(@Param("educationLevel") String educationLevel);

    /** 전체 학력 조회 */
    List<PolicyEducationLevelVO> findAllEducationLevels();

    /** 학력 마스터 저장 */
    void insertPolicyEducationLevel(PolicyEducationLevelVO vo);

    /** 정책-학력 연결 저장 */
    void insertYouthPolicyEducationLevel(YouthPolicyEducationLevelVO vo);

    // 취업 상태 관련

    /** 취업 상태 조회 */
    PolicyEmploymentStatusVO findEmploymentStatusByName(@Param("employmentStatus") String employmentStatus);

    /** 전체 취업 상태 조회 */
    List<PolicyEmploymentStatusVO> findAllEmploymentStatuses();

    /** 취업 상태 마스터 저장 */
    void insertPolicyEmploymentStatus(PolicyEmploymentStatusVO vo);

    /** 정책-취업 상태 연결 저장 */
    void insertYouthPolicyEmploymentStatus(YouthPolicyEmploymentStatusVO vo);

    // 특수 조건 관련

    /** 특수 조건 조회 */
    PolicySpecialConditionVO findSpecialConditionByName(@Param("specialCondition") String specialCondition);

    /** 전체 특수 조건 조회 */
    List<PolicySpecialConditionVO> findAllSpecialConditions();

    /** 특수 조건 마스터 저장 */
    void insertPolicySpecialCondition(PolicySpecialConditionVO vo);

    /** 정책-특수 조건 연결 저장 */
    void insertYouthPolicySpecialCondition(YouthPolicySpecialConditionVO vo);

    // 정책 상세 분리 조회
    YouthPolicyVO findYouthPolicyById(Long policyId);
    YouthPolicyConditionVO findYouthPolicyConditionByPolicyId(Long policyId);
    YouthPolicyPeriodVO findYouthPolicyPeriodByPolicyId(Long policyId);

    List<PolicyRegionVO> findRegionsByPolicyId(Long policyId);
    List<PolicyEducationLevelVO> findEducationLevelsByPolicyId(Long policyId);
    List<PolicyMajorVO> findMajorsByPolicyId(Long policyId);
    List<PolicyEmploymentStatusVO> findEmploymentStatusesByPolicyId(Long policyId);
    List<PolicySpecialConditionVO> findSpecialConditionsByPolicyId(Long policyId);
    List<PolicyKeywordVO> findKeywordsByPolicyId(Long policyId);

    // 정책 업데이트 관련

    /** 정책 조회수 업데이트 */
    void updatePolicyViews(@Param("policyNo") String policyNo, @Param("views") Long views);


    void insertPolicyVector(PolicyVectorVO vector);

    void updatePolicyVector(PolicyVectorVO vector);

    PolicyVectorVO findByPolicyId(Long policyId);

    Long findPolicyIdByPolicyNo(String policyNo);

    /** 당일 생성된 신규 정책 조회 */
    List<YouthPolicyVO> findTodayNewPolicies();

    // 마스터 테이블 전체 조회

    List<MasterPolicyRegionVO> findAllMasterRegions();
    List<MasterPolicyKeywordVO> findAllMasterKeywords();
    List<MasterPolicyMajorVO> findAllMasterMajors();
    List<MasterPolicyEducationLevelVO> findAllMasterEducationLevels();
    List<MasterPolicyEmploymentStatusVO> findAllMasterEmploymentStatuses();
    List<MasterPolicySpecialConditionVO> findAllMasterSpecialConditions();

    // ...기존 전체 조회 메서드는 필요시 deprecated 처리 또는 내부용으로 유지...
}
