package org.scoula.policy.dto;

import lombok.Data;
import java.util.List;

@Data
public class YouthPolicyResult {
    private PolicyPagging pagging;
    private List<PolicyDTO> youthPolicyList;
}
