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
import org.scoula.push.service.core.TokenCleanupService;
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
    private final TokenCleanupService tokenCleanupService;

    // ===============================
    // 알림 관련 API
    // ===============================

    /**
     * 알림 목록 조회
     */
    @GetMapping("/notifications")
    @ApiOperation(value = "전체 알림 목록 조회", 
                  notes = "현재 사용자의 모든 알림을 최신순으로 조회합니다. 읽음/미읽음 상태와 알림 타입(BOOKMARK/TOP3/NEW_POLICY/FEEDBACK)이 포함됩니다.")
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
                  notes = "현재 사용자의 특정 기기(토큰)에 대한 푸시 알림 구독 상태를 확인합니다. FCM 토큰은 필수 파라미터입니다.")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestParam String token) {
        Long userId = customUser.getMember().getUserId();
        SubscriptionStatusResponse status = subscriptionService.getSubscriptionStatus(userId, token);
        return ResponseEntity.ok(status);
    }

    // ===============================
    // 통합 알림 타입 토글 API
    // ===============================

    /**
     * 알림 타입별 토글 (통합 API)
     */
    @PutMapping("/subscriptions/{notificationType}")
    @ApiOperation(value = "알림 타입별 토글", 
                  notes = "지정된 알림 타입의 활성화/비활성화를 토글합니다. " +
                         "지원되는 타입: bookmark, top3, new-policy, feedback")
    public ResponseEntity<Void> toggleNotification(
            @PathVariable String notificationType,
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody NotificationToggleRequest request) {
        
        Long userId = customUser.getMember().getUserId();
        
        // 알림 타입 검증 및 해당 서비스 메서드 호출
        switch (notificationType.toLowerCase()) {
            case "bookmark":
                subscriptionService.toggleBookmarkNotification(userId, request);
                break;
            case "top3":
                subscriptionService.toggleTop3Notification(userId, request);
                break;
            case "new-policy":
                subscriptionService.toggleNewPolicyNotification(userId, request);
                break;
            case "feedback":
                subscriptionService.toggleFeedbackNotification(userId, request);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        
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

    @PostMapping("/admin/tokens/cleanup")
    @ApiOperation(value = "만료된 FCM 토큰 즉시 정리", 
                  notes = "최근 30일간 'Requested entity was not found' 에러로 3회 이상 실패한 FCM 토큰을 즉시 정리합니다.")
    public ResponseEntity<String> cleanupInvalidTokens() {
        try {
            tokenCleanupService.cleanupInvalidTokens();
            return ResponseEntity.ok("만료된 FCM 토큰 정리가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("토큰 정리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}