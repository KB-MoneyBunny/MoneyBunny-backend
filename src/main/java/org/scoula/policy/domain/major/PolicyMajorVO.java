package org.scoula.policy.domain.major;

import lombok.Data;
import org.scoula.policy.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Data
public class PolicyMajorVO {
    private Long id;
    private String major;
    
    public static List<PolicyMajorVO> fromCommaSeparated(String majorStr) {
        List<PolicyMajorVO> result = new ArrayList<>();
        if (StringUtils.hasText(majorStr)) {
            String[] majors = majorStr.split(",");
            for (String major : majors) {
                String trimmedMajor = major.trim();
                if (StringUtils.hasText(trimmedMajor)) {
                    PolicyMajorVO vo = new PolicyMajorVO();
                    vo.setMajor(trimmedMajor);
                    result.add(vo);
                }
            }
        }
        return result;
    }
}
