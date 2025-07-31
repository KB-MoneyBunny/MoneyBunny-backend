package org.scoula.push.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.domain.NotificationType;
import org.scoula.push.domain.Subscription;
import org.scoula.push.domain.UserNotification;
import org.scoula.push.dto.response.NotificationResponse;
import org.scoula.push.mapper.SubscriptionMapper;
import org.scoula.push.mapper.UserNotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 맞춤 알림 서비스
 * UserNotification 관리 및 FCM 발송을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final UserNotificationMapper userNotificationMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * 새로운 알림을 생성합니다
     */
    @Transactional
    public void createNotification(Long userId, String title, String message, 
                                 NotificationType type, String targetUrl) {
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        userNotificationMapper.insertNotification(notification);
        log.info("[알림 생성] 사용자 ID: {}, 제목: {}, 타입: {}", userId, title, type);
    }

    /**
     * 특정 사용자의 미읽은 알림 목록을 조회합니다
     */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<UserNotification> notifications = userNotificationMapper.findUnreadByUserId(userId);
        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 특정 사용자의 모든 알림 조회
     */
    public List<NotificationResponse> getNotifications(Long userId) {
        List<UserNotification> notifications = userNotificationMapper.findByUserId(userId);
        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 알림을 읽음 처리 (단순 버전)
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        userNotificationMapper.markAsRead(notificationId);
        log.info("[알림 읽음] 알림 ID: {}", notificationId);
    }

    /**
     * 특정 사용자의 미읽은 알림 개수를 조회합니다
     */
    public int getUnreadCount(Long userId) {
        return userNotificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 북마크 알림을 생성하고 FCM으로 즉시 발송합니다
     */
    @Transactional
    public void createAndSendBookmarkNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 알림 생성
        createNotification(userId, title, message, NotificationType.BOOKMARK, targetUrl);

        // 2. FCM 발송
        sendFCMToUser(userId, title, message);
    }

    /**
     * 피드백 알림을 생성하고 FCM으로 발송합니다
     */
    @Transactional
    public void createAndSendFeedbackNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 알림 생성
        createNotification(userId, title, message, NotificationType.FEEDBACK, targetUrl);

        // 2. FCM 발송
        sendFCMToUser(userId, title, message);
    }


    /**
     * 특정 사용자에게 FCM 발송
     */
    private void sendFCMToUser(Long userId, String title, String message) {
        // 해당 사용자의 구독 정보 조회
        Subscription subscription = subscriptionMapper.findByUserId(userId);
        
        if (subscription != null && subscription.getEndpoint() != null) {
            try {
                Notification notification = Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build();

                Message fcmMessage = Message.builder()
                        .setToken(subscription.getEndpoint())
                        .setNotification(notification)
                        .build();

                String response = firebaseMessaging.send(fcmMessage);
                log.info("[FCM 발송 성공] 사용자 ID: {}, 응답: {}", userId, response);
                
            } catch (FirebaseMessagingException e) {
                log.error("[FCM 발송 실패] 사용자 ID: {}, 오류: {}", userId, e.getMessage());
            }
        } else {
            log.warn("[FCM 발송 실패] 사용자 ID: {}의 구독 정보 또는 토큰이 없음", userId);
        }
    }


    // ==================== 스케줄러에서 호출할 메서드들 ====================

    /**
     * 개인 소비패턴 기반 피드백 알림 발송 (스케줄러용)
     */
    @Transactional
    public void triggerPersonalizedFeedback(Long userId) {
        log.info("[스케줄러] 개인 맞춤 피드백 알림 발송 - 사용자 ID: {}", userId);
        // 구현 예정
    }

    /**
     * 일괄 맞춤형 피드백 알림 발송 (배치용)
     */
    @Transactional
    public void triggerBatchPersonalizedFeedback() {
        log.info("[스케줄러] 일괄 맞춤형 피드백 알림 발송 시작");
        // 구현 예정
    }
}