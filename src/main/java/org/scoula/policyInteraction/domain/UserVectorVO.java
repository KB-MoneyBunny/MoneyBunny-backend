package org.scoula.policyInteraction.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 개인화 벡터 도메인
 * 3차원 벡터: 혜택금액, 마감일, 조회수 선호도 (합계=1.0, 최소값=0.1)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVectorVO {
    
    /** 기본키 */
    private Long id;
    
    /** 사용자 ID (외래키) */
    private Long userId;
    
    /** 혜택 금액 차원 벡터값 (0.1~1.0) */
    private BigDecimal vecBenefitAmount;
    
    /** 마감일 차원 벡터값 (0.1~1.0) */
    private BigDecimal vecDeadline;
    
    /** 조회수 차원 벡터값 (0.1~1.0) */
    private BigDecimal vecViews;
    
    /** 마지막 갱신 시각 */
    private LocalDateTime updatedAt;

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

