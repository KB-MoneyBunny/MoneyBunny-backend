package org.scoula.userPolicy.domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPolicyScoreVO {
    private Long userId;
    private Long policyId;
    private BigDecimal score;
}
