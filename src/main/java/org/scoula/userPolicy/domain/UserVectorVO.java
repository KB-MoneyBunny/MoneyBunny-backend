package org.scoula.userPolicy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVectorVO {
    private Long id;
    private Long userId;
    private BigDecimal vecBenefitAmount;
    private BigDecimal vecDeadline;
    private BigDecimal vecViews;
    private Timestamp updatedAt;
}
