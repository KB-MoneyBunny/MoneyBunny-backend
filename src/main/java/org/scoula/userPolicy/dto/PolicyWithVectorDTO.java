package org.scoula.userPolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 정책 정보와 벡터 정보를 함께 담는 DTO
 * 벡터 기반 추천 시스템에서 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyWithVectorDTO {
    // 기본 정책 정보 (SearchResultDTO와 동일)
    private Long policyId;
    private String title;
    private String policyBenefitDescription;
    private String endDate;
    private Long policyBenefitAmount;
    
    // 벡터 정보 (policy_vector 테이블에서 조회)
    private BigDecimal vecBenefitAmount;  // 혜택금액 벡터
    private BigDecimal vecDeadline;       // 마감일 벡터
    private BigDecimal vecViews;          // 조회수 벡터
    
    // 계산된 유사도
    private Double similarity;            // 코사인 유사도 점수
    
    /**
     * 벡터를 double 배열로 변환 (null 안전)
     * 기존 UserVectorVO, PolicyVectorVO와 동일한 방식
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