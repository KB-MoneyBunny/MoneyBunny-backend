package org.scoula.push.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.push.domain.UserNotification;
import org.scoula.push.dto.request.SubscriptionRequest;
import org.scoula.push.dto.response.SubscriptionStatusResponse;
import org.scoula.push.service.SubscriptionService;
import org.scoula.push.service.UserNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 통합 푸시 알림 API Controller
 * - 알림 조회/관리
 * - 구독 관리
 * - 관리자 기능
 */
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final UserNotificationService userNotificationService;
    private final SubscriptionService subscriptionService;

    // ===============================
    // 알림 관련 API
    // ===============================

    /**
     * 알림 목록 조회 (타입별 필터링 가능)
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<UserNotification>> getNotifications(
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        List<UserNotification> notifications = userNotificationService.getNotificationsByType(userId, type);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 조회
     */
    @GetMapping("/notifications/unread")
    public ResponseEntity<List<UserNotification>> getUnreadNotifications(HttpServletRequest request) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        List<UserNotification> notifications = userNotificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 개수
     */
    @GetMapping("/notifications/unread/count")
    public ResponseEntity<Integer> getUnreadCount(HttpServletRequest request) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        int count = userNotificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        userNotificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // ===============================
    // 구독 관련 API
    // ===============================

    /**
     * 알림 구독 등록
     */
    @PostMapping("/subscriptions")
    public ResponseEntity<Void> subscribe(@RequestBody SubscriptionRequest request, HttpServletRequest httpRequest) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        subscriptionService.subscribe(userId, request.getToken());
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 구독 해제
     */
    @DeleteMapping("/subscriptions/{token}")
    public ResponseEntity<Void> unsubscribe(@PathVariable String token) {
        subscriptionService.unsubscribe(token);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 구독 상태 조회
     */
    @GetMapping("/subscriptions/status")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(HttpServletRequest httpRequest) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        boolean isSubscribed = subscriptionService.isSubscribed(userId);
        return ResponseEntity.ok(SubscriptionStatusResponse.of(isSubscribed));
    }

    // ===============================
    // 관리자 API
    // ===============================

    /**
     * 알림 발송 통계 조회 (관리자용)
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<String> getNotificationStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        String stats = userNotificationService.getNotificationStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}