package org.scoula.policy.domain.employment;

import lombok.Data;
import org.scoula.policy.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Data
public class PolicyEmploymentStatusVO {
    private Long id;
    private String employmentStatus;
    
    public static List<PolicyEmploymentStatusVO> fromCommaSeparated(String employmentStatusStr) {
        List<PolicyEmploymentStatusVO> result = new ArrayList<>();
        if (StringUtils.hasText(employmentStatusStr)) {
            String[] employmentStatuses = employmentStatusStr.split(",");
            for (String employmentStatus : employmentStatuses) {
                String trimmedEmploymentStatus = employmentStatus.trim();
                if (StringUtils.hasText(trimmedEmploymentStatus)) {
                    PolicyEmploymentStatusVO vo = new PolicyEmploymentStatusVO();
                    vo.setEmploymentStatus(trimmedEmploymentStatus);
                    result.add(vo);
                }
            }
        }
        return result;
    }
}
