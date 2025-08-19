package org.scoula.push.service.core;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final SubscriptionMapper subscriptionMapper;

    public void sendAllCustomNotifications() {
        log.info("[FCM] 맞춤 알림 전송 작업을 시작합니다.");
        // 모든 알림 타입의 활성 구독자를 조회 (테스트용이므로 FEEDBACK 타입으로 통일)
        List<SubscriptionVO> subscriptions = subscriptionMapper.findActiveByNotificationType("FEEDBACK");
        log.info("[FCM] DB에서 {}개의 활성 구독을 찾았습니다.", subscriptions.size());

        if (subscriptions.isEmpty()) {
            log.warn("[FCM] 전송할 활성 구독이 없어 작업을 중단합니다.");
            return;
        }

        for (SubscriptionVO subscription : subscriptions) {
            String endpoint = subscription.getFcmToken();
            Long userId = subscription.getUserId();

            log.debug("[FCM] 사용자 ID {}에게 알림 전송을 시도합니다. 토큰: {}", userId, endpoint);

            // 테스트용 하드코딩된 메시지
            String title = "머니버니 알림";
            String body = "안녕하세요 사용자 #" + userId + "님, 맞춤 정책이 도착했어요!";

            Message message = Message.builder()
                    .setToken(endpoint)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("targetUrl", "/")
                    .build();

            try {
                String response = firebaseMessaging.send(message);
                log.info("[FCM] 사용자 {}에게 성공적으로 메시지를 보냈습니다. FCM 응답: {}", userId, response);
            } catch (FirebaseMessagingException e) {
                // FCM에서 제공하는 에러 코드를 확인하여 원인을 파악합니다.
                // https://firebase.google.com/docs/cloud-messaging/manage-tokens#detect-invalid-token-responses-from-the-fcm-backend
                MessagingErrorCode errorCode = e.getMessagingErrorCode();
                if (errorCode == MessagingErrorCode.UNREGISTERED) {
                    log.error("[FCM] 사용자 {}의 토큰이 유효하지 않거나 만료되었습니다. 토큰: {}", userId, endpoint);
                } else {
                    log.error("[FCM] 사용자 {}에게 메시지 전송 실패. 토큰: {}, 에러 코드: {}", userId, endpoint, errorCode, e);
                }
            }
        }
        log.info("[FCM] 맞춤 알림 전송 작업을 완료했습니다.");
    }
}
