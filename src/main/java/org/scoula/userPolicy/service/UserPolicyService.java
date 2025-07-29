package org.scoula.userPolicy.service;

import org.scoula.userPolicy.domain.UserPolicyConditionVO;
import org.scoula.userPolicy.dto.UserPolicyDTO;

import java.util.List;

public interface UserPolicyService {
    UserPolicyDTO saveUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO);

    UserPolicyDTO updateUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO);
}
