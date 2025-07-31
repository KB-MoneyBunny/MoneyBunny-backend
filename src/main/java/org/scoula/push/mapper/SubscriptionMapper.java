package org.scoula.push.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.push.domain.Subscription;

import java.util.List;

public interface SubscriptionMapper {

    // FCM 토큰으로 기존 구독 정보 조회
    Subscription findByToken(@Param("token") String token);

    // 새 구독 정보 저장
    void insert(Subscription subscription);

    // 알림 유형별 구독 설정 업데이트
    void updateNotificationSettings(Subscription subscription);

    // 특정 알림 타입의 활성 구독자 조회
    List<Subscription> findActiveByNotificationType(@Param("notificationType") String notificationType);

    // 사용자 ID로 구독 정보 조회
    Subscription findByUserId(@Param("userId") Long userId);

    // 사용자의 구독 상태 확인 (하나라도 활성화되어 있으면 true)
    boolean isUserSubscribed(@Param("userId") Long userId);
}
