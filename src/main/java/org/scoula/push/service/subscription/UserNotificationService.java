package org.scoula.push.service.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.domain.NotificationType;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.domain.UserNotificationVO;
import org.scoula.push.dto.response.NotificationResponse;
import org.scoula.push.mapper.SubscriptionMapper;
import org.scoula.push.mapper.UserNotificationMapper;
import org.scoula.push.service.core.AsyncNotificationService;
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
    private final AsyncNotificationService asyncNotificationService;

    /**
     * 새로운 알림을 생성하고 ID를 반환합니다 (개선된 버전)
     */
    @Transactional
    public Long createNotification(Long userId, String title, String message,
                                   NotificationType type, String targetUrl) {
        UserNotificationVO notification = UserNotificationVO.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        userNotificationMapper.insertNotification(notification);
        log.info("[알림 생성] 사용자 ID: {}, 제목: {}, 타입: {}, 알림 ID: {}", userId, title, type, notification.getId());

        return notification.getId();
    }

    /**
     * 특정 사용자의 미읽은 알림 목록을 조회합니다
     */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<UserNotificationVO> notifications = userNotificationMapper.findUnreadByUserId(userId);
        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 특정 사용자의 모든 알림 조회
     */
    public List<NotificationResponse> getNotifications(Long userId) {
        List<UserNotificationVO> notifications = userNotificationMapper.findByUserId(userId);
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
     * 북마크 알림을 동기 생성 후 비동기 FCM 발송 (개선된 버전)
     */
    @Transactional
    public void createAndSendBookmarkNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 동기: 알림 생성 (DB 저장)
        Long notificationId = createNotification(userId, title, message, NotificationType.BOOKMARK, targetUrl);

        // 2. 비동기: FCM 발송 (로그 기록 포함)
        sendFCMToUserAsync(notificationId, userId, title, message);
    }

    /**
     * 피드백 알림을 동기 생성 후 비동기 FCM 발솠 (개선된 버전)
     */
    @Transactional
    public void createAndSendFeedbackNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 동기: 알림 생성 (DB 저장)
        Long notificationId = createNotification(userId, title, message, NotificationType.FEEDBACK, targetUrl);

        // 2. 비동기: FCM 발송 (로그 기록 포함)
        sendFCMToUserAsync(notificationId, userId, title, message);
    }

    /**
     * 신규 정책 알림을 동기 생성 후 비동기 FCM 발송
     */
    @Transactional
    public void createAndSendNewPolicyNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 동기: 알림 생성 (DB 저장)
        Long notificationId = createNotification(userId, title, message, NotificationType.NEW_POLICY, targetUrl);

        // 2. 비동기: FCM 발송 (로그 기록 포함)
        sendFCMToUserAsync(notificationId, userId, title, message);
    }


    /**
     * 비동기 FCM 발송 (로그 기록 포함) - 개선된 버전
     */
    private void sendFCMToUserAsync(Long notificationId, Long userId, String title, String message) {
        // 해당 사용자의 구독 정보 조회
        SubscriptionVO subscription = subscriptionMapper.findByUserId(userId);

        if (subscription != null && subscription.getFcmToken() != null) {
            // 비동기 FCM 발송 (로그 자동 기록)
            asyncNotificationService.sendFCMWithLogging(notificationId, subscription.getFcmToken(), title, message);
            log.debug("[비동기 FCM 발송 시작] 사용자 ID: {}, 알림 ID: {}", userId, notificationId);
        } else {
            log.warn("[FCM 발송 실패] 사용자 ID: {}의 구독 정보 또는 토큰이 없음", userId);
        }
    }

}