package org.scoula.policy.util;

import lombok.RequiredArgsConstructor;
import org.scoula.policy.domain.master.*;
import org.scoula.policy.mapper.PolicyMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
        loadMasterRegions();
        loadMasterKeywords();
        loadMasterMajors();
        loadMasterEducationLevels();
        loadMasterEmploymentStatuses();
        loadMasterSpecialConditions();
    }

    private void loadMasterRegions() {
        List<MasterPolicyRegionVO> regions = policyMapper.findAllMasterRegions();
        for (MasterPolicyRegionVO region : regions) {
            regionMap.put(region.getRegionCode(), region.getId());
            regionNameMap.put(region.getId(), region.getRegionCode());
        }
    }

    private void loadMasterKeywords() {
        List<MasterPolicyKeywordVO> keywords = policyMapper.findAllMasterKeywords();
        for (MasterPolicyKeywordVO keyword : keywords) {
            keywordMap.put(keyword.getKeyword(), keyword.getId());
            keywordNameMap.put(keyword.getId(), keyword.getKeyword());
        }
    }

    private void loadMasterMajors() {
        List<MasterPolicyMajorVO> majors = policyMapper.findAllMasterMajors();
        for (MasterPolicyMajorVO major : majors) {
            majorMap.put(major.getMajor(), major.getId());
            majorNameMap.put(major.getId(), major.getMajor());
        }
    }

    private void loadMasterEducationLevels() {
        List<MasterPolicyEducationLevelVO> educationLevels = policyMapper.findAllMasterEducationLevels();
        for (MasterPolicyEducationLevelVO educationLevel : educationLevels) {
            educationLevelMap.put(educationLevel.getEducationLevel(), educationLevel.getId());
            educationLevelNameMap.put(educationLevel.getId(), educationLevel.getEducationLevel());
        }
    }

    private void loadMasterEmploymentStatuses() {
        List<MasterPolicyEmploymentStatusVO> employmentStatuses = policyMapper.findAllMasterEmploymentStatuses();
        for (MasterPolicyEmploymentStatusVO employmentStatus : employmentStatuses) {
            employmentStatusMap.put(employmentStatus.getEmploymentStatus(), employmentStatus.getId());
            employmentStatusNameMap.put(employmentStatus.getId(), employmentStatus.getEmploymentStatus());
        }
    }

    private void loadMasterSpecialConditions() {
        List<MasterPolicySpecialConditionVO> specialConditions = policyMapper.findAllMasterSpecialConditions();
        for (MasterPolicySpecialConditionVO specialCondition : specialConditions) {
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
}
