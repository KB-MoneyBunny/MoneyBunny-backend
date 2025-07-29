package org.scoula.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyVectorVO {
    private Long id;
    private Long policyId;
    private BigDecimal vecBenefitAmount;
    private BigDecimal vecDeadline;
    private BigDecimal vecViews;
    private LocalDateTime createdAt;

    /**
     * 벡터를 double 배열로 변환 (null 안전)
     * @return 3차원 double 배열 [혜택금액, 마감일, 조회수]
     */
    public double[] toArray() {
        return new double[]{
                vecBenefitAmount != null ? vecBenefitAmount.doubleValue() : 0.0,
                vecDeadline != null ? vecDeadline.doubleValue() : 0.0,
                vecViews != null ? vecViews.doubleValue() : 0.0
        };
    }

}