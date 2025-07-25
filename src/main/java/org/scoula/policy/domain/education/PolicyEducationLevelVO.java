package org.scoula.policy.domain.education;

import lombok.Data;
import org.scoula.policy.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Data
public class PolicyEducationLevelVO {
    private Long id;
    private String educationLevel;
    
    public static List<PolicyEducationLevelVO> fromCommaSeparated(String educationLevelStr) {
        List<PolicyEducationLevelVO> result = new ArrayList<>();
        if (StringUtils.hasText(educationLevelStr)) {
            String[] educationLevels = educationLevelStr.split(",");
            for (String educationLevel : educationLevels) {
                String trimmedEducationLevel = educationLevel.trim();
                if (StringUtils.hasText(trimmedEducationLevel)) {
                    PolicyEducationLevelVO vo = new PolicyEducationLevelVO();
                    vo.setEducationLevel(trimmedEducationLevel);
                    result.add(vo);
                }
            }
        }
        return result;
    }
}
