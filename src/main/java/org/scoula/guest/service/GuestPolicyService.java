package org.scoula.guest.service;

import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;

import java.util.List;

public interface GuestPolicyService {
    List<SearchResultDTO> searchGuestPolicies(SearchRequestDTO searchRequestDTO);
}
