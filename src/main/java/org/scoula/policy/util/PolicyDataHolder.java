package org.scoula.policy.util;

import lombok.RequiredArgsConstructor;
import org.scoula.policy.domain.education.PolicyEducationLevelVO;
import org.scoula.policy.domain.employment.PolicyEmploymentStatusVO;
import org.scoula.policy.domain.keyword.PolicyKeywordVO;
import org.scoula.policy.domain.major.PolicyMajorVO;
import org.scoula.policy.domain.region.PolicyRegionVO;
import org.scoula.policy.domain.specialcondition.PolicySpecialConditionVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PolicyDataHolder {

    private final PolicyMapper policyMapper;

    private final Map<String, Long> regionMap = new ConcurrentHashMap<>();
    private final Map<String, Long> keywordMap = new ConcurrentHashMap<>();
    private final Map<String, Long> majorMap = new ConcurrentHashMap<>();
    private final Map<String, Long> educationLevelMap = new ConcurrentHashMap<>();
    private final Map<String, Long> employmentStatusMap = new ConcurrentHashMap<>();
    private final Map<String, Long> specialConditionMap = new ConcurrentHashMap<>();

    private final Map<Long, String> regionNameMap = new ConcurrentHashMap<>();
    private final Map<Long, String> keywordNameMap = new ConcurrentHashMap<>();
    private final Map<Long, String> majorNameMap = new ConcurrentHashMap<>();
    private final Map<Long, String> educationLevelNameMap = new ConcurrentHashMap<>();
    private final Map<Long, String> employmentStatusNameMap = new ConcurrentHashMap<>();
    private final Map<Long, String> specialConditionNameMap = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        loadRegions();
        loadKeywords();
        loadMajors();
        loadEducationLevels();
        loadEmploymentStatuses();
        loadSpecialConditions();
    }

    private void loadRegions() {
        List<PolicyRegionVO> regions = policyMapper.findAllRegions();
        for (PolicyRegionVO region : regions) {
            regionMap.put(region.getRegionCode(), region.getId());
            regionNameMap.put(region.getId(), region.getRegionCode());
        }
    }

    private void loadKeywords() {
        List<PolicyKeywordVO> keywords = policyMapper.findAllKeywords();
        for (PolicyKeywordVO keyword : keywords) {
            keywordMap.put(keyword.getKeyword(), keyword.getId());
            keywordNameMap.put(keyword.getId(), keyword.getKeyword());
        }
    }

    private void loadMajors() {
        List<PolicyMajorVO> majors = policyMapper.findAllMajors();
        for (PolicyMajorVO major : majors) {
            majorMap.put(major.getMajor(), major.getId());
            majorNameMap.put(major.getId(), major.getMajor());
        }
    }

    private void loadEducationLevels() {
        List<PolicyEducationLevelVO> educationLevels = policyMapper.findAllEducationLevels();
        for (PolicyEducationLevelVO educationLevel : educationLevels) {
            educationLevelMap.put(educationLevel.getEducationLevel(), educationLevel.getId());
            educationLevelNameMap.put(educationLevel.getId(), educationLevel.getEducationLevel());
        }
    }

    private void loadEmploymentStatuses() {
        List<PolicyEmploymentStatusVO> employmentStatuses = policyMapper.findAllEmploymentStatuses();
        for (PolicyEmploymentStatusVO employmentStatus : employmentStatuses) {
            employmentStatusMap.put(employmentStatus.getEmploymentStatus(), employmentStatus.getId());
            employmentStatusNameMap.put(employmentStatus.getId(), employmentStatus.getEmploymentStatus());
        }
    }

    private void loadSpecialConditions() {
        List<PolicySpecialConditionVO> specialConditions = policyMapper.findAllSpecialConditions();
        for (PolicySpecialConditionVO specialCondition : specialConditions) {
            specialConditionMap.put(specialCondition.getSpecialCondition(), specialCondition.getId());
            specialConditionNameMap.put(specialCondition.getId(), specialCondition.getSpecialCondition());
        }
    }

    public Long getRegionId(String name) {
        return regionMap.get(name);
    }

    public Long getKeywordId(String name) {
        return keywordMap.get(name);
    }

    public Long getMajorId(String name) {
        return majorMap.get(name);
    }

    public Long getEducationLevelId(String name) {
        return educationLevelMap.get(name);
    }

    public Long getEmploymentStatusId(String name) {
        return employmentStatusMap.get(name);
    }

    public Long getSpecialConditionId(String name) {
        return specialConditionMap.get(name);
    }

    public String getRegionName(Long id) {
        return regionNameMap.get(id);
    }

    public String getKeywordName(Long id) {
        return keywordNameMap.get(id);
    }

    public String getMajorName(Long id) {
        return majorNameMap.get(id);
    }

    public String getEducationLevelName(Long id) {
        return educationLevelNameMap.get(id);
    }

    public String getEmploymentStatusName(Long id) {
        return employmentStatusNameMap.get(id);
    }

    public String getSpecialConditionName(Long id) {
        return specialConditionNameMap.get(id);
    }



    public void putRegion(String name, Long id) {
        regionMap.put(name, id);
    }

    public void putKeyword(String name, Long id) {
        keywordMap.put(name, id);
    }

    public void putMajor(String name, Long id) {
        majorMap.put(name, id);
    }

    public void putEducationLevel(String name, Long id) {
        educationLevelMap.put(name, id);
    }

    public void putEmploymentStatus(String name, Long id) {
        employmentStatusMap.put(name, id);
    }

    public void putSpecialCondition(String name, Long id) {
        specialConditionMap.put(name, id);
    }

    /**
     * 주어진 prefix(예: "41")로 시작하는 모든 지역코드 반환
     */
    public List<String> getRegionCodesByPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        for (String code : regionMap.keySet()) {
            if (code.startsWith(prefix)) {
                result.add(code);
            }
        }
        return result;
    }
}
