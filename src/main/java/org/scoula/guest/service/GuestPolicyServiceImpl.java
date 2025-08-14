package org.scoula.guest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.util.PolicyDataHolder;
import org.scoula.userPolicy.dto.PolicyWithVectorDTO;
import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.mapper.UserPolicyMapper;
import org.scoula.userPolicy.util.VectorUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestPolicyServiceImpl implements GuestPolicyService {

    private final UserPolicyMapper userPolicyMapper;
    private final PolicyDataHolder policyDataHolder;

    @Override
    public List<SearchResultDTO> searchGuestPolicies(SearchRequestDTO searchRequestDTO) {
        log.info("비로그인 사용자 검색 요청: 벡터 기반 정렬 없이 필터링만 적용");

        // 빈 문자열을 제거하는 유틸 메서드
        searchRequestDTO.setRegions(removeEmptyStrings(searchRequestDTO.getRegions()));
        searchRequestDTO.setEducationLevels(removeEmptyStrings(searchRequestDTO.getEducationLevels()));
        searchRequestDTO.setEmploymentStatuses(removeEmptyStrings(searchRequestDTO.getEmploymentStatuses()));
        searchRequestDTO.setMajors(removeEmptyStrings(searchRequestDTO.getMajors()));
        searchRequestDTO.setSpecialConditions(removeEmptyStrings(searchRequestDTO.getSpecialConditions()));
        searchRequestDTO.setKeywords(removeEmptyStrings(searchRequestDTO.getKeywords()));

        // 지역 코드 확장 로직 (두 단계)
        List<String> originalRegions = searchRequestDTO.getRegions();
        Set<String> expandedRegionNames = new HashSet<>();
        for (String name : originalRegions) {
            if (name.length() == 5 && name.endsWith("000")) {
                String prefix = name.substring(0, 2);
                expandedRegionNames.addAll(policyDataHolder.getRegionCodesByPrefix(prefix));
            } else {
                expandedRegionNames.add(name);
            }
            if (name.length() >= 2) {
                String generalizedRegion = name.substring(0, 2) + "000";
                expandedRegionNames.add(generalizedRegion);
            }
            expandedRegionNames.add(name);
        }
        searchRequestDTO.setRegions(new ArrayList<>(expandedRegionNames));

        List<PolicyWithVectorDTO> policiesWithVectors = userPolicyMapper.findFilteredPoliciesWithVectors(searchRequestDTO);
        return policiesWithVectors.stream()
                .map(VectorUtil::toSearchResultDTO)
                .collect(Collectors.toList());
    }

    /**
     * 주어진 리스트에서 빈 문자열을 제거하는 유틸리티 메소드.
     * @param list 문자열 리스트
     * @return 빈 문자열이 제거된 리스트
     */
    private List<String> removeEmptyStrings(List<String> list) {
        if (list == null) return null;
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .toList();
    }
}
