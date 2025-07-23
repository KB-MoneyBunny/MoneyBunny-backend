// PushNotificationService.java
package org.scoula.push.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.scoula.push.domain.Subscription;
import org.scoula.push.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final SubscriptionMapper subscriptionMapper;

    public void sendAllCustomNotifications() {
        List<Subscription> subscriptions = subscriptionMapper.findAllActive();

        for (Subscription subscription : subscriptions) {
            String endpoint = subscription.getEndpoint();
            Long userId = subscription.getUserId();

            // TODO: 여기에 사용자 맞춤 로직 넣기 (지금은 하드코딩)
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
                System.out.println("✅ 사용자 " + userId + " 전송 성공: " + response);
            } catch (FirebaseMessagingException e) {
                System.err.println("❌ 사용자 " + userId + " 전송 실패: " + e.getMessage());
            }
        }
    }
}
