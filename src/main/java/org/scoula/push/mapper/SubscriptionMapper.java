package org.scoula.push.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.push.domain.Subscription;

import java.util.List;

public interface SubscriptionMapper {

    // FCM 토큰으로 기존 구독 정보 조회
    Subscription findByToken(@Param("token") String token);

    // 새 구독 정보 저장
    void insert(Subscription subscription);

    // 구독 활성화/비활성화 상태 업데이트
    void updateIsActive(@Param("token") String token, @Param("isActive") boolean isActive);

    // SubscriptionMapper.java
    List<Subscription> findAllActive();
}
