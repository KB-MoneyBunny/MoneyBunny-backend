package org.scoula.policy.service;

import org.scoula.policy.dto.PolicyDetailDTO;

public interface PolicyService {
    void fetchAndSaveAllPolicies();

    PolicyDetailDTO getPolicyById(String policyId);
    
    /**
     * 사용자용 정책 상세 조회 (조회 추적 + 벡터 갱신 포함)
     * @param policyId 정책 ID
     * @param userId 사용자 ID (인증된 사용자)
     * @return 정책 상세 정보
     */
    PolicyDetailDTO getPolicyDetailWithTracking(String policyId, Long userId);
}
