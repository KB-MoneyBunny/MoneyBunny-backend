package org.scoula.push.controller;


import lombok.RequiredArgsConstructor;
import org.scoula.push.dto.PushNotificationRequest;
import org.scoula.push.dto.SubscriptionRequest;
import org.scoula.push.service.PushNotificationService;
import org.scoula.push.service.SubscriptionService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final SubscriptionService subscriptionService;
    private final PushNotificationService pushNotificationService;

//    private final JwtUtil jwtUtil;

    /**
     * FCM 토큰 구독 등록 (최초 등록 또는 재활성화)
     */
    @PostMapping("/subscribe")
    public void subscribe(@RequestBody SubscriptionRequest request,
                          HttpServletRequest httpRequest) {
// JWT에서 사용자 ID 추출 (반드시 로그인 사용자 기준)
//        Long userId = jwtUtil.extractUserId(httpRequest);
        Long userId = 1L;  // ✅ 테스트용 하드코딩
        subscriptionService.subscribe(userId, request.getToken());
    }

    /**
     * FCM 구독 해제 (is_active = false 처리)
     */
    @PostMapping("/unsubscribe")
    public void unsubscribe(@RequestBody SubscriptionRequest request) {
        subscriptionService.unsubscribe(request.getToken());
    }

    // 전송 테스트용도
    @PostMapping("/send-test")
    public String sendTestNotification() {
        pushNotificationService.sendAllCustomNotifications();
        return "테스트용 푸시 전송 완료";
    }


    /**
     * 알림 클릭 시 user_notification 읽음 처리
     */
//    @PostMapping("/clicked")
//    public void clicked(@RequestParam Long id) {
//        userNotificationService.markAsRead(id);
//    }
}
