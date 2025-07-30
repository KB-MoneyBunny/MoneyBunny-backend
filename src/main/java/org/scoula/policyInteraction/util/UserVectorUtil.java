package org.scoula.policyInteraction.util;

import org.scoula.policyInteraction.domain.UserVectorVO;

import java.math.BigDecimal;

/**
 * 사용자 벡터 관련 공통 유틸리티
 */
public class UserVectorUtil {

    /**
     * 초기 사용자 벡터 생성 (중립값)
     * @param userId 사용자 ID
     * @return 초기화된 사용자 벡터 (0.5, 0.5, 0.5)
     */
    public static UserVectorVO createInitialUserVector(Long userId) {
        return UserVectorVO.builder()
            .userId(userId)
            .vecBenefitAmount(new BigDecimal("0.5"))
            .vecDeadline(new BigDecimal("0.5"))
            .vecViews(new BigDecimal("0.5"))
            .build();
    }
}