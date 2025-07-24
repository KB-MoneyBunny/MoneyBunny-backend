package org.scoula.push.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.push.domain.UserNotification;
import org.scoula.push.service.UserNotificationService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *  사용자 알림 조회 API
 *   - 맞춤 알림 목록 조회 (정책/피드백 타입별)
 *   - 읽지 않은 알림 조회
 *   - 알림 읽음 처리
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationQueryController {

    private final UserNotificationService userNotificationService;

    /**
     * 읽지 않은 맞춤 알림 조회
     */
    @GetMapping("/unread")
    public List<UserNotification> getUnreadNotifications(HttpServletRequest request) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        return userNotificationService.getUnreadNotifications(userId);
    }

    /**
     * 알림 타입별 조회 (정책 알림/소비패턴 피드백)
     */
    @GetMapping
    public List<UserNotification> getNotifications(
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        return userNotificationService.getNotificationsByType(userId, type);
    }

    /**
     * 읽지 않은 알림 개수
     */
    @GetMapping("/unread/count")
    public int getUnreadCount(HttpServletRequest request) {
        // TODO: JWT에서 사용자 ID 추출
        Long userId = 1L;
        return userNotificationService.getUnreadCount(userId);
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId) {
        userNotificationService.markAsRead(notificationId);
    }
}
