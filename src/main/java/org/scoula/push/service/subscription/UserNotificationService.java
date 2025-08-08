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
     * 알림 삭제 (사용자 권한 확인 포함)
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        // 먼저 알림이 해당 사용자의 것인지 확인
        UserNotificationVO notification = userNotificationMapper.findById(notificationId);
        
        if (notification == null) {
            log.warn("[알림 삭제 실패] 알림을 찾을 수 없음 - 알림 ID: {}", notificationId);
            throw new IllegalArgumentException("알림을 찾을 수 없습니다.");
        }
        
        if (!notification.getUserId().equals(userId)) {
            log.warn("[알림 삭제 실패] 권한 없음 - 알림 ID: {}, 요청 사용자 ID: {}, 실제 소유자 ID: {}", 
                     notificationId, userId, notification.getUserId());
            throw new IllegalArgumentException("해당 알림을 삭제할 권한이 없습니다.");
        }
        
        userNotificationMapper.deleteById(notificationId);
        log.info("[알림 삭제] 알림 ID: {}, 사용자 ID: {}", notificationId, userId);
    }

    /**
     * 북마크 알림을 동기 생성 후 비동기 FCM 발송 (개선된 버전)
     */
    @Transactional
    public void createAndSendBookmarkNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 동기: 알림 생성 (DB 저장)
        Long notificationId = createNotification(userId, title, message, NotificationType.BOOKMARK, targetUrl);

        // 2. 비동기: 북마크 전용 FCM 발송 (여러 토큰 지원)
        sendBookmarkFCMToUserAsync(notificationId, userId, title, message);
    }

    /**
     * 피드백 알림을 동기 생성 후 비동기 FCM 발솠 (개선된 버전)
     */
    @Transactional
    public void createAndSendFeedbackNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 동기: 알림 생성 (DB 저장)
        Long notificationId = createNotification(userId, title, message, NotificationType.FEEDBACK, targetUrl);

        // 2. 비동기: 피드백 전용 FCM 발송 (여러 토큰 지원)
        sendFeedbackFCMToUserAsync(notificationId, userId, title, message);
    }

    /**
     * 신규 정책 알림을 동기 생성 후 비동기 FCM 발송
     */
    @Transactional
    public void createAndSendNewPolicyNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 동기: 알림 생성 (DB 저장)
        Long notificationId = createNotification(userId, title, message, NotificationType.NEW_POLICY, targetUrl);

        // 2. 비동기: 신규 정책 전용 FCM 발송 (여러 토큰 지원)
        sendNewPolicyFCMToUserAsync(notificationId, userId, title, message);
    }

    /**
     * TOP3 알림을 동기 생성 후 비동기 FCM 발송
     */
    @Transactional
    public void createAndSendTop3Notification(Long userId, String title, String message, String targetUrl) {
        // 1. 동기: 알림 생성 (DB 저장)
        Long notificationId = createNotification(userId, title, message, NotificationType.TOP3, targetUrl);

        // 2. 비동기: TOP3 전용 FCM 발송 (여러 토큰 지원)
        sendTop3FCMToUserAsync(notificationId, userId, title, message);
    }


    /**
     * 북마크 알림 비동기 FCM 발송 (여러 토큰 지원)
     */
    private void sendBookmarkFCMToUserAsync(Long notificationId, Long userId, String title, String message) {
        // 해당 사용자의 북마크 알림이 활성화된 모든 토큰 조회
        List<String> activeTokens = subscriptionMapper.findActiveBookmarkTokensByUserId(userId);

        if (activeTokens.isEmpty()) {
            log.warn("[북마크 FCM 발송 실패] 사용자 ID: {}의 활성화된 북마크 알림 토큰이 없음", userId);
            return;
        }

        log.info("[북마크 FCM 발송] 사용자 ID: {}에 대해 {}개의 활성 토큰 발견", userId, activeTokens.size());

        // 각 토큰에 대해 비동기 FCM 발송
        for (String token : activeTokens) {
            asyncNotificationService.sendFCMWithLogging(notificationId, token, title, message);
            log.debug("[북마크 비동기 FCM 발송 시작] 사용자 ID: {}, 알림 ID: {}, 토큰: {}...", 
                     userId, notificationId, token.substring(0, Math.min(20, token.length())));
        }
    }

    /**
     * TOP3 알림 비동기 FCM 발송 (여러 토큰 지원)
     */
    private void sendTop3FCMToUserAsync(Long notificationId, Long userId, String title, String message) {
        List<String> activeTokens = subscriptionMapper.findActiveTop3TokensByUserId(userId);

        if (activeTokens.isEmpty()) {
            log.warn("[TOP3 FCM 발송 실패] 사용자 ID: {}의 활성화된 TOP3 알림 토큰이 없음", userId);
            return;
        }

        log.info("[TOP3 FCM 발송] 사용자 ID: {}에 대해 {}개의 활성 토큰 발견", userId, activeTokens.size());

        for (String token : activeTokens) {
            asyncNotificationService.sendFCMWithLogging(notificationId, token, title, message);
            log.debug("[TOP3 비동기 FCM 발송 시작] 사용자 ID: {}, 알림 ID: {}, 토큰: {}...", 
                     userId, notificationId, token.substring(0, Math.min(20, token.length())));
        }
    }

    /**
     * 신규 정책 알림 비동기 FCM 발송 (여러 토큰 지원)
     */
    private void sendNewPolicyFCMToUserAsync(Long notificationId, Long userId, String title, String message) {
        List<String> activeTokens = subscriptionMapper.findActiveNewPolicyTokensByUserId(userId);

        if (activeTokens.isEmpty()) {
            log.warn("[신규 정책 FCM 발송 실패] 사용자 ID: {}의 활성화된 신규 정책 알림 토큰이 없음", userId);
            return;
        }

        log.info("[신규 정책 FCM 발송] 사용자 ID: {}에 대해 {}개의 활성 토큰 발견", userId, activeTokens.size());

        for (String token : activeTokens) {
            asyncNotificationService.sendFCMWithLogging(notificationId, token, title, message);
            log.debug("[신규 정책 비동기 FCM 발송 시작] 사용자 ID: {}, 알림 ID: {}, 토큰: {}...", 
                     userId, notificationId, token.substring(0, Math.min(20, token.length())));
        }
    }

    /**
     * 피드백 알림 비동기 FCM 발송 (여러 토큰 지원)
     */
    private void sendFeedbackFCMToUserAsync(Long notificationId, Long userId, String title, String message) {
        List<String> activeTokens = subscriptionMapper.findActiveFeedbackTokensByUserId(userId);

        if (activeTokens.isEmpty()) {
            log.warn("[피드백 FCM 발송 실패] 사용자 ID: {}의 활성화된 피드백 알림 토큰이 없음", userId);
            return;
        }

        log.info("[피드백 FCM 발송] 사용자 ID: {}에 대해 {}개의 활성 토큰 발견", userId, activeTokens.size());

        for (String token : activeTokens) {
            asyncNotificationService.sendFCMWithLogging(notificationId, token, title, message);
            log.debug("[피드백 비동기 FCM 발송 시작] 사용자 ID: {}, 알림 ID: {}, 토큰: {}...", 
                     userId, notificationId, token.substring(0, Math.min(20, token.length())));
        }
    }

}