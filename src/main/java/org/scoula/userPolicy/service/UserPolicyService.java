package org.scoula.userPolicy.service;

import org.scoula.userPolicy.dto.UserPolicyDTO;

import java.util.List;

public interface UserPolicyService {
    UserPolicyDTO saveUserPolicy(String username, UserPolicyDTO userPolicyDTO);
    UserPolicyDTO getUserPolicy(String username);
    List<String> getCustomizedPolicyIds(String username);
}
