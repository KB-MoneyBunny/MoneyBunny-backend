package org.scoula.policy.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.scoula.policy.domain.*;

public interface PolicyMapper {

    // 정책 개수 확인
    int countAllPolicies();
    // 정책 존재 여부 확인
    boolean existsByPolicyNo(@Param("policyNo") String policyNo);

    // 정책 저장
    void insertPolicy(YouthPolicyVO policyVO);

    // 조건 저장
    void insertCondition(YouthPolicyConditionVO conditionVO);

    // 기간 저장
    void insertPeriod(YouthPolicyPeriodVO periodVO);

    // 키워드 조회
    PolicyKeywordVO findKeywordByName(@Param("keyword") String keyword);

    // 키워드 저장
    void insertKeyword(PolicyKeywordVO keywordVO);

    // 정책-키워드 관계 저장
    void insertPolicyKeyword(@Param("policyId") Long policyId,
                             @Param("keywordId") Long keywordId);

    PolicyRegionVO findRegionByCode(@Param("regionCode") String regionCode);

    // 지역 코드 마스터 저장
    void insertRegion(PolicyRegionVO regionVO);

    // 정책-지역 관계 저장
    void insertPolicyRegion(@Param("policyId") Long policyId,
                            @Param("regionId") Long regionId);
}
