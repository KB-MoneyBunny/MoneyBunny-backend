package org.scoula.push.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.push.dto.request.NotificationToggleRequest;
import org.scoula.push.dto.request.SubscriptionRequest;
import org.scoula.push.dto.response.NotificationResponse;
import org.scoula.push.dto.response.SubscriptionStatusResponse;
import org.scoula.push.service.notification.BookmarkPolicyNotificationService;
import org.scoula.push.service.notification.FeedbackNotificationService;
import org.scoula.push.service.notification.NewPolicyNotificationService;
import org.scoula.push.service.notification.Top3NotificationService;
import org.scoula.push.service.core.PushNotificationService;
import org.scoula.push.service.subscription.SubscriptionService;
import org.scoula.push.service.subscription.UserNotificationService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    private final NewPolicyNotificationService newPolicyNotificationService;
    private final Top3NotificationService top3NotificationService;
    private final FeedbackNotificationService feedbackNotificationService;

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
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
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
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
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
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
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
     * FCM 토큰 초기 등록 및 전체 알림 설정
     */
    @PostMapping("/subscriptions")
    @ApiOperation(value = "FCM 토큰 등록 및 초기 알림 설정", 
                  notes = "앱 최초 설치 시 FCM 토큰을 등록하고 모든 알림 타입의 초기 설정을 한번에 진행합니다.")
    public ResponseEntity<Void> registerFcmToken(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody SubscriptionRequest request) {
        Long userId = customUser.getMember().getUserId();
        subscriptionService.subscribe(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 구독 상태 조회
     */
    @GetMapping("/subscriptions/status")
    @ApiOperation(value = "푸시 알림 구독 상태 확인", 
                  notes = "현재 사용자의 푸시 알림 구독 여부를 확인합니다. 활성 토큰이 있으면 true, 없으면 false를 반환합니다.")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        SubscriptionStatusResponse status = subscriptionService.getSubscriptionStatus(userId);
        return ResponseEntity.ok(status);
    }

    // ===============================
    // 개별 알림 타입 토글 API (실무에서 많이 사용하는 방식)
    // ===============================

    /**
     * 북마크 알림 토글
     */
    @PutMapping("/subscriptions/bookmark")
    @ApiOperation(value = "북마크 알림 토글", 
                  notes = "북마크 정책 알림의 활성화/비활성화를 즉시 토글합니다.")
    public ResponseEntity<Void> toggleBookmarkNotification(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody NotificationToggleRequest request) {
        Long userId = customUser.getMember().getUserId();
        subscriptionService.toggleBookmarkNotification(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * TOP3 알림 토글
     */
    @PutMapping("/subscriptions/top3")
    @ApiOperation(value = "TOP3 알림 토글", 
                  notes = "TOP3 정책 추천 알림의 활성화/비활성화를 즉시 토글합니다.")
    public ResponseEntity<Void> toggleTop3Notification(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody NotificationToggleRequest request) {
        Long userId = customUser.getMember().getUserId();
        subscriptionService.toggleTop3Notification(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 신규 정책 알림 토글
     */
    @PutMapping("/subscriptions/new-policy")
    @ApiOperation(value = "신규 정책 알림 토글", 
                  notes = "신규 정책 알림의 활성화/비활성화를 즉시 토글합니다.")
    public ResponseEntity<Void> toggleNewPolicyNotification(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody NotificationToggleRequest request) {
        Long userId = customUser.getMember().getUserId();
        subscriptionService.toggleNewPolicyNotification(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 피드백 알림 토글
     */
    @PutMapping("/subscriptions/feedback")
    @ApiOperation(value = "피드백 알림 토글", 
                  notes = "개인 맞춤 피드백 알림의 활성화/비활성화를 즉시 토글합니다.")
    public ResponseEntity<Void> toggleFeedbackNotification(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody NotificationToggleRequest request) {
        Long userId = customUser.getMember().getUserId();
        subscriptionService.toggleFeedbackNotification(userId, request);
        return ResponseEntity.ok().build();
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

    @PostMapping("/admin/new-policy/send")
    @ApiOperation(value = "신규 정책 알림 수동 발송",
                  notes = "오늘 생성된 정책 중 사용자 조건에 맞는 정책들에 대해 신규 정책 알림을 즉시 발송합니다.")
    public ResponseEntity<String> sendNewPolicyNotifications() {
        try {
            newPolicyNotificationService.processNewPolicyAlerts();
            return ResponseEntity.ok("신규 정책 알림이 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("신규 정책 알림 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/top3/send")
    @ApiOperation(value = "TOP3 정책 추천 알림 수동 발송",
                  notes = "모든 TOP3 구독자에게 개인화된 TOP3 정책 추천 알림을 즉시 발송합니다.")
    public ResponseEntity<String> sendTop3Notifications() {
        try {
            top3NotificationService.sendTop3Notifications();
            return ResponseEntity.ok("TOP3 정책 추천 알림이 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("TOP3 알림 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/feedback/send") 
    @ApiOperation(value = "피드백 알림 수동 발송",
                  notes = "모든 FEEDBACK 구독자에게 주간 소비 리포트(지출 비교 + 요일별 피크 분석) 알림을 즉시 발송합니다.")
    public ResponseEntity<String> sendFeedbackNotifications() {
        try {
            feedbackNotificationService.sendWeeklyConsumptionReportToAll();
            return ResponseEntity.ok("피드백 알림이 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("피드백 알림 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}