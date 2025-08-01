package org.scoula.push.service;

import lombok.RequiredArgsConstructor;
import org.scoula.push.domain.NotificationType;
import org.scoula.push.domain.SubscriptionVO;
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
            // 기존 구독이 있으면 알림 설정 업데이트
            existing.setIsActiveBookmark(request.getBookmarkSetting());
            existing.setIsActiveTop3(request.getTop3Setting());
            existing.setIsActiveNewPolicy(request.getNewPolicySetting());
            existing.setIsActiveFeedback(request.getFeedbackSetting());
            subscriptionMapper.updateNotificationSettings(existing);
        } else {
            // 신규 구독 생성
            SubscriptionVO subscription = new SubscriptionVO();
            subscription.setUserId(userId);
            subscription.setFcmToken(request.getToken());
            subscription.setIsActiveBookmark(request.getBookmarkSetting());
            subscription.setIsActiveTop3(request.getTop3Setting());
            subscription.setIsActiveNewPolicy(request.getNewPolicySetting());
            subscription.setIsActiveFeedback(request.getFeedbackSetting());
            subscription.setCreatedAt(LocalDateTime.now());
            subscriptionMapper.insert(subscription);
        }
    }

    /**
     * 알림 설정 업데이트
     */
    public void updateNotificationSettings(Long userId, SubscriptionRequest request) {
        SubscriptionVO existing = subscriptionMapper.findByUserId(userId);
        if (existing != null) {
            existing.setFcmToken(request.getToken()); // 토큰도 업데이트
            existing.setIsActiveBookmark(request.getBookmarkSetting());
            existing.setIsActiveTop3(request.getTop3Setting());
            existing.setIsActiveNewPolicy(request.getNewPolicySetting());
            existing.setIsActiveFeedback(request.getFeedbackSetting());
            subscriptionMapper.updateNotificationSettings(existing);
        }
    }

    /**
     * 사용자의 구독 상태 조회
     */
    public SubscriptionStatusResponse getSubscriptionStatus(Long userId) {
        SubscriptionVO subscription = subscriptionMapper.findByUserId(userId);
        return SubscriptionStatusResponse.from(subscription);
    }

    /**
     * 사용자의 현재 구독 여부 확인
     */
    public boolean isSubscribed(Long userId) {
        return subscriptionMapper.isUserSubscribed(userId);
    }

}
