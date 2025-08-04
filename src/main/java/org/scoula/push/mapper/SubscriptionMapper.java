package org.scoula.push.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.push.domain.SubscriptionVO;

import java.util.List;

public interface SubscriptionMapper {

    // FCM 토큰으로 기존 구독 정보 조회
    SubscriptionVO findByToken(@Param("token") String token);

    // 새 구독 정보 저장
    void insert(SubscriptionVO subscription);

    // 알림 유형별 구독 설정 업데이트
    void updateNotificationSettings(SubscriptionVO subscription);

    // 특정 알림 타입의 활성 구독자 조회
    List<SubscriptionVO> findActiveByNotificationType(@Param("notificationType") String notificationType);

    // 사용자 ID로 구독 정보 조회
    SubscriptionVO findByUserId(@Param("userId") Long userId);
    
    // 사용자 ID와 토큰으로 특정 기기의 구독 정보 조회
    SubscriptionVO findByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);

    // 사용자의 구독 상태 확인 (하나라도 활성화되어 있으면 true)
    boolean isUserSubscribed(@Param("userId") Long userId);

    // 신규 정책 알림 활성 구독자 조회
    List<SubscriptionVO> findActiveNewPolicySubscribers();
}
