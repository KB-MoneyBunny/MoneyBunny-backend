package org.scoula.userPolicy.service;

import org.scoula.userPolicy.dto.UserPolicyDTO;

import java.util.List;

public interface UserPolicyService {
    UserPolicyDTO saveUserPolicy(String username, UserPolicyDTO userPolicyDTO);
    List<String> returnUserPolicyIdList(String username);
}
