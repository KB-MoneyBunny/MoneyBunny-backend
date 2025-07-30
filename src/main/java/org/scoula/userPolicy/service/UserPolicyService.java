package org.scoula.userPolicy.service;

import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.dto.UserPolicyDTO;

import java.util.List;


public interface UserPolicyService {
    UserPolicyDTO saveUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO);

    UserPolicyDTO getUserPolicyCondition(String username);

    UserPolicyDTO updateUserPolicyCondition(String username, UserPolicyDTO userPolicyDTO);

    List<SearchResultDTO> searchMatchingPolicy(String username);

    List<SearchResultDTO> searchFilteredPolicy(String username, SearchRequestDTO searchRequestDTO);
}
