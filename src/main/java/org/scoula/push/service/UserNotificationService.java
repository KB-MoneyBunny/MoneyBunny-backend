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
                .type(type.name())
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
    public List<UserNotification> getUnreadNotifications(Long userId) {
        return userNotificationMapper.findUnreadByUserId(userId);
    }

    /**
     * 특정 사용자의 모든 알림 조회
     */
    public List<UserNotification> getNotifications(Long userId) {
        return userNotificationMapper.findByUserId(userId);
    }

    /**
     * 알림 타입별 조회 (정책 알림/소비패턴 피드백)
     */
    public List<UserNotification> getNotificationsByType(Long userId, String type) {
        if (type == null || type.isEmpty()) {
            return userNotificationMapper.findByUserId(userId);
        }
        return userNotificationMapper.findByUserIdAndType(userId, type);
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
     * 정책 알림을 생성하고 FCM으로 발송합니다
     */
    @Transactional
    public void createAndSendPolicyNotification(Long userId, String title, String message, String targetUrl) {
        // 1. 알림 생성
        createNotification(userId, title, message, NotificationType.POLICY, targetUrl);

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
        // 해당 사용자의 활성 구독 정보 조회
        List<Subscription> subscriptions = subscriptionMapper.findAllActive();
        
        for (Subscription subscription : subscriptions) {
            if (subscription.getUserId().equals(userId)) {
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
            }
        }
    }

    // ==================== 관리자용 메서드들 ====================

    /**
     * 알림 발송 통계 조회
     */
    public String getNotificationStats(String startDate, String endDate) {
        // TODO: 기간별 알림 발송 통계 조회
        return "통계 데이터 준비 중";
    }

    // ==================== 스케줄러에서 호출할 메서드들 ====================

    /**
     * 북마크한 정책의 오픈/마감 알림 발송 (스케줄러용)
     */
    @Transactional
    public void triggerBookmarkedPolicyNotifications(Long policyId) {
        // TODO: 해당 정책을 북마크한 모든 사용자 조회
        // TODO: 정책 상태 확인 (오픈/마감)
        // TODO: 맞춤형 정책 알림 생성 및 발송
        log.info("[스케줄러] 북마크 정책 알림 발송 - 정책 ID: {}", policyId);
    }

    /**
     * 개인 소비패턴 기반 피드백 알림 발송 (스케줄러용)
     */
    @Transactional
    public void triggerPersonalizedFeedback(Long userId) {
        // TODO: 사용자의 최근 소비패턴 분석
        // TODO: 맞춤형 피드백 메시지 생성
        // TODO: 피드백 알림 생성 및 발송
        log.info("[스케줄러] 개인 맞춤 피드백 알림 발송 - 사용자 ID: {}", userId);
    }

    /**
     * 일괄 맞춤형 피드백 알림 발송 (배치용)
     */
    @Transactional
    public void triggerBatchPersonalizedFeedback() {
        // TODO: 피드백 대상 사용자 조회 (최근 활동 기준)
        // TODO: 각 사용자별 맞춤형 피드백 생성
        // TODO: 일괄 알림 발송
        log.info("[스케줄러] 일괄 맞춤형 피드백 알림 발송 시작");
    }
}