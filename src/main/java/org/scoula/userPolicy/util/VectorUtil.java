package org.scoula.userPolicy.util;

import org.scoula.userPolicy.domain.UserVectorVO;
import org.scoula.userPolicy.dto.PolicyWithVectorDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;

/**
 * 벡터 연산 관련 유틸리티 클래스
 */
public class VectorUtil {

    /**
     * 코사인 유사도 계산
     * 공식: cos(θ) = (A·B) / (|A| × |B|)
     * 
     * @param userVector 사용자 벡터
     * @param policyWithVector 정책 벡터가 포함된 DTO
     * @return 코사인 유사도 (0.0 ~ 1.0)
     */
    public static double calculateCosineSimilarity(UserVectorVO userVector, PolicyWithVectorDTO policyWithVector) {
        if (userVector == null || policyWithVector == null) {
            return 0.0;
        }
        
        // 기존 toArray() 메서드 활용
        double[] userVec = userVector.toArray();
        double[] policyVec = policyWithVector.toArray();
        
        // 내적(dot product) 계산
        double dotProduct = 0.0;
        for (int i = 0; i < userVec.length; i++) {
            dotProduct += userVec[i] * policyVec[i];
        }
        
        // 각 벡터의 크기(magnitude) 계산
        double userMagnitude = calculateMagnitude(userVec);
        double policyMagnitude = calculateMagnitude(policyVec);
        
        // 0으로 나누기 방지
        if (userMagnitude == 0.0 || policyMagnitude == 0.0) {
            return 0.0;
        }
        
        // 코사인 유사도 = 내적 / (크기1 × 크기2)
        double similarity = dotProduct / (userMagnitude * policyMagnitude);
        
        // 결과값을 0~1 범위로 제한 (음수 방지)
        return Math.max(0.0, Math.min(1.0, similarity));
    }
    
    /**
     * 벡터의 크기(magnitude) 계산
     * |A| = √(a1² + a2² + a3²)
     */
    private static double calculateMagnitude(double[] vector) {
        double sumOfSquares = 0.0;
        for (double value : vector) {
            sumOfSquares += value * value;
        }
        return Math.sqrt(sumOfSquares);
    }
    
    /**
     * PolicyWithVectorDTO를 SearchResultDTO로 변환
     * @param policyWithVector 벡터 정보가 포함된 DTO
     * @return 기존 SearchResultDTO 형식
     */
    public static SearchResultDTO toSearchResultDTO(PolicyWithVectorDTO policyWithVector) {
        return SearchResultDTO.builder()
                .policyId(policyWithVector.getPolicyId())
                .title(policyWithVector.getTitle())
                .policyBenefitDescription(policyWithVector.getPolicyBenefitDescription())
                .endDate(policyWithVector.getEndDate())
                .build();
    }
}