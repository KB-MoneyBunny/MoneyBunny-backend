package org.scoula.policy.service;

import org.scoula.policy.dto.PolicyDetailDTO;

public interface PolicyService {
    void fetchAndSaveAllPolicies();

    PolicyDetailDTO getPolicyById(String policyId);
}
