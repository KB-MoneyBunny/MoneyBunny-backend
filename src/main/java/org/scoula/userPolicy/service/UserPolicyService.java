package org.scoula.userPolicy.service;

import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.dto.TestResultRequestDTO;

import java.util.List;


public interface UserPolicyService {
    /**
     * 사용자 정책 조건을 조회합니다.
     *
     * @param username 사용자 이름
     * @return 사용자 정책 조건 DTO
     */
    TestResultRequestDTO getUserPolicyCondition(String username);

    /**
     * 사용자 정책 조건을 저장합니다.
     *
     * @param username 사용자 이름
     * @param testResultRequestDTO 사용자 정책 DTO
     * @return 저장된 사용자 정책 DTO
     */
    TestResultRequestDTO saveUserPolicyCondition(String username, TestResultRequestDTO testResultRequestDTO);

    /**
     * 사용자 정책 조건을 업데이트합니다.
     *
     * @param username 사용자 이름
     * @param testResultRequestDTO 사용자 정책 DTO
     * @return 업데이트된 사용자 정책 DTO
     */
    TestResultRequestDTO updateUserPolicyCondition(String username, TestResultRequestDTO testResultRequestDTO);

    /**
     * 사용자 정책 조건에 따라 필터링된 정책 목록을 조회합니다.
     *
     * @param username 사용자 이름
     * @return 사용자 맞춤 정책 목록
     */
    List<SearchResultDTO> searchMatchingPolicy(String username);

    /**
     * 사용자 선택한 조건과 작성한 검색어에 따라 필터링된 정책 목록을 조회합니다.
     *
     * @param username 사용자 이름
     * @param searchRequestDTO 검색 요청 DTO
     * @return 필터링된 정책 목록
     */
    List<SearchResultDTO> searchFilteredPolicy(String username, SearchRequestDTO searchRequestDTO);

    void saveSearchText(String searchText);

    List<String> getPopularKeywords(int count);

    void saveRecentSearch(String username, String searchText);

    List<String> getRecentSearches(String username);

    void deleteUserPolicyCondition(String username);

    void deleteRecentSearches(String username);

    void deleteRecentSearch(String username, String keyword);
}
