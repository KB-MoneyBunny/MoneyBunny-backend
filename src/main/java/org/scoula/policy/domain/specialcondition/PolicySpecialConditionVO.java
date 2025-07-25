package org.scoula.policy.domain.specialcondition;

import lombok.Data;
import org.scoula.policy.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Data
public class PolicySpecialConditionVO {
    private Long id;
    private String specialCondition;
    
    public static List<PolicySpecialConditionVO> fromCommaSeparated(String specialConditionStr) {
        List<PolicySpecialConditionVO> result = new ArrayList<>();
        if (StringUtils.hasText(specialConditionStr)) {
            String[] specialConditions = specialConditionStr.split(",");
            for (String specialCondition : specialConditions) {
                String trimmedSpecialCondition = specialCondition.trim();
                if (StringUtils.hasText(trimmedSpecialCondition)) {
                    PolicySpecialConditionVO vo = new PolicySpecialConditionVO();
                    vo.setSpecialCondition(trimmedSpecialCondition);
                    result.add(vo);
                }
            }
        }
        return result;
    }
}
