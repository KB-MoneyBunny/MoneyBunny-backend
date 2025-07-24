package org.scoula.push.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.push.dto.request.SubscriptionRequest;
import org.scoula.push.dto.response.SubscriptionStatusResponse;
import org.scoula.push.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * FCM 구독 관리 API
 *  - 맞춤형 알림 구독/해제
 */
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 맞춤형 알림 구독
     */
    @PostMapping("/subscribe")
    public void subscribe(@RequestBody SubscriptionRequest request, HttpServletRequest httpRequest) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        // TODO: FCM 토큰 등록 및 맞춤형 알림 구독 설정
        subscriptionService.subscribe(userId, request.getToken());
    }

    /**
     * 맞춤형 알림 구독 해제
     */
    @PostMapping("/unsubscribe")
    public void unsubscribe(@RequestBody SubscriptionRequest request) {
        // TODO: FCM 토큰 기반 구독 해제
        subscriptionService.unsubscribe(request.getToken());
    }

    /**
     * 현재 구독 상태 조회
     */
    @GetMapping("/status")
    public SubscriptionStatusResponse getSubscriptionStatus(HttpServletRequest httpRequest) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        boolean isSubscribed = subscriptionService.isSubscribed(userId);
        return SubscriptionStatusResponse.of(isSubscribed);
    }
}
