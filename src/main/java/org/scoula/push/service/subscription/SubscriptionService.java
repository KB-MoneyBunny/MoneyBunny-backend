package org.scoula.push.service.subscription;

import lombok.RequiredArgsConstructor;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.dto.request.NotificationToggleRequest;
import org.scoula.push.dto.request.SubscriptionRequest;
import org.scoula.push.dto.response.SubscriptionStatusResponse;
import org.scoula.push.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;

    /**
     * 구독 요청 처리 (알림 유형별 설정 포함)
     */
    public void subscribe(Long userId, SubscriptionRequest request) {
        SubscriptionVO existing = subscriptionMapper.findByToken(request.getToken());

        if (existing != null) {
            // 기존 구독이 있으면 userId 및 알림 설정 업데이트
            existing.setUserId(userId);
            existing.setIsActiveBookmark(request.isActiveBookmark());
            existing.setIsActiveTop3(request.isActiveTop3());
            existing.setIsActiveNewPolicy(request.isActiveNewPolicy());
            existing.setIsActiveFeedback(request.isActiveFeedback());
            subscriptionMapper.updateNotificationSettings(existing);
        } else {
            // 신규 구독 생성
            SubscriptionVO subscription = new SubscriptionVO();
            subscription.setUserId(userId);
            subscription.setFcmToken(request.getToken());
            subscription.setIsActiveBookmark(request.isActiveBookmark());
            subscription.setIsActiveTop3(request.isActiveTop3());
            subscription.setIsActiveNewPolicy(request.isActiveNewPolicy());
            subscription.setIsActiveFeedback(request.isActiveFeedback());
            subscription.setCreatedAt(LocalDateTime.now());
            subscriptionMapper.insert(subscription);
        }
    }


    /**
     * 사용자의 구독 상태 조회
     * @param userId 사용자 ID
     * @param token FCM 토큰 (필수) - 해당 기기의 설정 조회
     */
    public SubscriptionStatusResponse getSubscriptionStatus(Long userId, String token) {
        // 토큰으로 해당 기기의 구독 정보 조회
        SubscriptionVO subscription = subscriptionMapper.findByToken(token);
        
        if (subscription == null) {
            return SubscriptionStatusResponse.from(null);
        }
        
        // 구독정보의 userId가 현재 사용자와 다르면 새로운 사용자로 초기화
        if (!subscription.getUserId().equals(userId)) {
            subscription.setUserId(userId);
            subscription.setIsActiveBookmark(false);
            subscription.setIsActiveTop3(false);
            subscription.setIsActiveNewPolicy(false);
            subscription.setIsActiveFeedback(false);
            subscriptionMapper.updateNotificationSettings(subscription);
        }
        
        return SubscriptionStatusResponse.from(subscription);
    }

    /**
     * 사용자의 현재 구독 여부 확인
     */
    public boolean isSubscribed(Long userId) {
        return subscriptionMapper.isUserSubscribed(userId);
    }

    // ================================
    // 개별 알림 타입 토글 메서드들
    // ================================

    /**
     * 북마크 알림 토글
     */
    public void toggleBookmarkNotification(Long userId, NotificationToggleRequest request) {
        SubscriptionVO subscription = getOrCreateSubscription(userId, request.getToken());
        subscription.setIsActiveBookmark(request.isEnabled());
        subscriptionMapper.updateNotificationSettings(subscription);
    }

    /**
     * TOP3 알림 토글
     */
    public void toggleTop3Notification(Long userId, NotificationToggleRequest request) {
        SubscriptionVO subscription = getOrCreateSubscription(userId, request.getToken());
        subscription.setIsActiveTop3(request.isEnabled());
        subscriptionMapper.updateNotificationSettings(subscription);
    }

    /**
     * 신규 정책 알림 토글
     */
    public void toggleNewPolicyNotification(Long userId, NotificationToggleRequest request) {
        SubscriptionVO subscription = getOrCreateSubscription(userId, request.getToken());
        subscription.setIsActiveNewPolicy(request.isEnabled());
        subscriptionMapper.updateNotificationSettings(subscription);
    }

    /**
     * 피드백 알림 토글
     */
    public void toggleFeedbackNotification(Long userId, NotificationToggleRequest request) {
        SubscriptionVO subscription = getOrCreateSubscription(userId, request.getToken());
        subscription.setIsActiveFeedback(request.isEnabled());
        subscriptionMapper.updateNotificationSettings(subscription);
    }

    /**
     * 구독 정보 조회 또는 생성 (헬퍼 메서드)
     */
    private SubscriptionVO getOrCreateSubscription(Long userId, String token) {
        SubscriptionVO existing = subscriptionMapper.findByToken(token);
        if (existing != null) {
            return existing;
        }

        // 신규 구독 생성 (모든 알림 비활성화 상태로 시작)
        SubscriptionVO newSubscription = new SubscriptionVO();
        newSubscription.setUserId(userId);
        newSubscription.setFcmToken(token);
        newSubscription.setIsActiveBookmark(false);
        newSubscription.setIsActiveTop3(false);
        newSubscription.setIsActiveNewPolicy(false);
        newSubscription.setIsActiveFeedback(false);
        newSubscription.setCreatedAt(LocalDateTime.now());
        subscriptionMapper.insert(newSubscription);
        
        return newSubscription;
    }

    /**
     * FCM 토큰으로 구독 해제
     */
    public void unsubscribe(String token) {
        SubscriptionVO existing = subscriptionMapper.findByToken(token);
        if (existing != null) {
            subscriptionMapper.deleteByToken(token);
        }
    }

}
