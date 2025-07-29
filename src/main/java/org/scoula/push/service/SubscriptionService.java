package org.scoula.push.service;

import lombok.RequiredArgsConstructor;
import org.scoula.push.domain.Subscription;
import org.scoula.push.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;

    /**
     * 구독 요청 처리: 이미 존재하면 활성화, 없으면 신규 등록
     */
    public void subscribe(Long userId, String token) {
        Subscription existing = subscriptionMapper.findByToken(token);

        if (existing != null) {
            subscriptionMapper.updateIsActive(token, true);
        } else {
            Subscription sub = new Subscription();
            sub.setUserId(userId);
            sub.setEndpoint(token);
            sub.setActive(true);
            sub.setCreatedAt(LocalDateTime.now());
            subscriptionMapper.insert(sub);
        }
    }

    /**
     * 구독 해제 처리
     */
    public void unsubscribe(Long userId, String token) {
        subscriptionMapper.updateIsActive(token, false);
    }
    
    /**
     * 구독 해제 처리 (토큰만으로)
     */
    public void unsubscribe(String token) {
        subscriptionMapper.updateIsActive(token, false);
    }

    /**
     * 사용자의 현재 구독 상태 조회
     */
    public boolean isSubscribed(Long userId) {
        // TODO: 사용자의 현재 구독 상태 반환
        return subscriptionMapper.isUserSubscribed(userId);
    }
}
