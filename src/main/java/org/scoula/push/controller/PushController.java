package org.scoula.push.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.push.dto.request.SubscriptionRequest;
import org.scoula.push.dto.response.NotificationResponse;
import org.scoula.push.dto.response.SubscriptionStatusResponse;
import org.scoula.push.service.BookmarkPolicyNotificationService;
import org.scoula.push.service.PushNotificationService;
import org.scoula.push.service.SubscriptionService;
import org.scoula.push.service.UserNotificationService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
@Api(tags = "푸시 알림 API", description = "FCM 기반 푸시 알림 및 인앱 알림 관리")
public class PushController {

    private final UserNotificationService userNotificationService;
    private final SubscriptionService subscriptionService;
    private final PushNotificationService pushNotificationService;
    private final BookmarkPolicyNotificationService bookmarkPolicyNotificationService;

    // ===============================
    // 알림 관련 API
    // ===============================

    /**
     * 알림 목록 조회
     */
    @GetMapping("/notifications")
    @ApiOperation(value = "전체 알림 목록 조회", 
                  notes = "현재 사용자의 모든 알림을 최신순으로 조회합니다. 읽음/미읽음 상태와 알림 타입(POLICY/FEEDBACK)이 포함됩니다.")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        List<NotificationResponse> notifications = userNotificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 조회
     */
    @GetMapping("/notifications/unread")
    @ApiOperation(value = "미읽은 알림 목록 조회", 
                  notes = "읽지 않은 알림만 필터링하여 조회합니다. 새로운 알림 확인 시 사용합니다.")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        List<NotificationResponse> notifications = userNotificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 개수
     */
    @GetMapping("/notifications/unread/count")
    @ApiOperation(value = "미읽은 알림 개수 조회", 
                  notes = "읽지 않은 알림의 총 개수를 반환합니다. 앱 아이콘 뱃지나 알림 카운터 표시에 사용합니다.")
    public ResponseEntity<Integer> getUnreadCount(
            @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        int count = userNotificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/notifications/{notificationId}/read")
    @ApiOperation(value = "알림 읽음 처리", 
                  notes = "특정 알림을 읽음 상태로 변경합니다. 성공 시 200 OK를 반환합니다.")
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
    @ApiOperation(value = "푸시 알림 구독 등록", 
                  notes = "FCM 토큰을 등록하여 푸시 알림을 구독합니다. 이미 등록된 토큰인 경우 활성화 상태로 변경됩니다.")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody SubscriptionRequest request) {
        Long userId = customUser.getMember().getUserId();
        subscriptionService.subscribe(userId, request.getToken());
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 구독 해제
     */
    @DeleteMapping("/subscriptions")
    @ApiOperation(value = "푸시 알림 구독 해제", 
                  notes = "FCM 토큰을 비활성화하여 푸시 알림 수신을 중단합니다. 토큰은 삭제되지 않고 비활성 상태로 변경됩니다.")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody SubscriptionRequest request) {
        Long userId = customUser.getMember().getUserId();
        subscriptionService.unsubscribe(userId, request.getToken());
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 구독 상태 조회
     */
    @GetMapping("/subscriptions/status")
    @ApiOperation(value = "푸시 알림 구독 상태 확인", 
                  notes = "현재 사용자의 푸시 알림 구독 여부를 확인합니다. 활성 토큰이 있으면 true, 없으면 false를 반환합니다.")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(
            @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        boolean isSubscribed = subscriptionService.isSubscribed(userId);
        return ResponseEntity.ok(SubscriptionStatusResponse.of(isSubscribed));
    }

    // ===============================
    // 관리자 API
    // ===============================


    @PostMapping("/admin/send-test")
    @ApiOperation(value = "전체 사용자에게 푸시 알림 테스트", notes = "현재 DB에 등록된 모든 사용자에게 테스트 알림을 전송합니다.")
    public ResponseEntity<String> sendTestPush() {
        pushNotificationService.sendAllCustomNotifications();
        return ResponseEntity.ok("테스트 푸시 알림을 전송했습니다.");
    }

    @PostMapping("/admin/bookmark-policy/send")
    @ApiOperation(value = "북마크 정책 알림 수동 발송", 
                  notes = "모든 사용자의 북마크된 정책에 대해 신청일/마감일 알림을 즉시 발송합니다.")
    public ResponseEntity<String> sendBookmarkPolicyNotifications() {
        try {
            bookmarkPolicyNotificationService.checkAndSendBookmarkNotifications();
            return ResponseEntity.ok("북마크 기반 정책 알림이 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("알림 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/feedback/send") 
    @ApiOperation(value = "피드백 알림 수동 발송",
                  notes = "모든 사용자에게 개인 맞춤 피드백 알림을 발송합니다. 현재는 미구현 상태입니다.")
    public ResponseEntity<String> sendFeedbackNotifications() {
        try {
            userNotificationService.triggerBatchPersonalizedFeedback();
            return ResponseEntity.ok("피드백 알림이 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("피드백 알림 발송 중 오료가 발생했습니다: " + e.getMessage());
        }
    }
}