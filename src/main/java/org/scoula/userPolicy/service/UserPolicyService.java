package org.scoula.userPolicy.service;

import org.scoula.userPolicy.dto.UserPolicyDTO;


public interface UserPolicyService {
    UserPolicyDTO saveUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO);

    UserPolicyDTO updateUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO);
}
