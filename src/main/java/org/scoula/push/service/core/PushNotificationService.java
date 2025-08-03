// PushNotificationService.java
package org.scoula.push.service.core;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final SubscriptionMapper subscriptionMapper;

    public void sendAllCustomNotifications() {
        System.out.println("ℹ️ [FCM] 맞춤 알림 전송 작업을 시작합니다.");
        // 모든 알림 타입의 활성 구독자를 조회 (테스트용이므로 FEEDBACK 타입으로 통일)
        List<SubscriptionVO> subscriptions = subscriptionMapper.findActiveByNotificationType("FEEDBACK");
        System.out.println("ℹ️ [FCM] DB에서 " + subscriptions.size() + "개의 활성 구독을 찾았습니다.");

        if (subscriptions.isEmpty()) {
            System.out.println("⚠️ [FCM] 전송할 활성 구독이 없어 작업을 중단합니다.");
            return;
        }

        for (SubscriptionVO subscription : subscriptions) {
            String endpoint = subscription.getFcmToken();
            Long userId = subscription.getUserId();

            System.out.println("ℹ️ [FCM] 사용자 ID " + userId + "에게 알림 전송을 시도합니다. 토큰: " + endpoint);

            // 테스트용 하드코딩된 메시지
            String title = "💰 머니버니 알림";
            String body = "안녕하세요 사용자 #" + userId + "님, 맞춤 정책이 도착했어요!";

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(endpoint)
                    .setNotification(notification)
                    .build();

            try {
                String response = firebaseMessaging.send(message);
                System.out.println("✅ [FCM] 사용자 " + userId + "에게 성공적으로 메시지를 보냈습니다. FCM 응답: " + response);
            } catch (FirebaseMessagingException e) {
                // FCM에서 제공하는 에러 코드를 확인하여 원인을 파악합니다.
                // https://firebase.google.com/docs/cloud-messaging/manage-tokens#detect-invalid-token-responses-from-the-fcm-backend
                MessagingErrorCode errorCode = e.getMessagingErrorCode();
                if (errorCode == MessagingErrorCode.UNREGISTERED) {
                    System.err.println("❌ [FCM] 사용자 " + userId + "의 토큰이 유효하지 않거나 만료되었습니다. 토큰: " + endpoint);
                } else {
                    System.err.println("❌ [FCM] 사용자 " + userId + "에게 메시지 전송 실패. 토큰: " + endpoint + ", 에러 코드: " + errorCode);
                    e.printStackTrace(); // 전체 스택 트레이스를 출력하여 더 자세한 정보를 확인합니다.
                }
            }
        }
        System.out.println("ℹ️ [FCM] 맞춤 알림 전송 작업을 완료했습니다.");
    }
}
