package org.scoula.push.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.push.service.UserNotificationService;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 관리 API (관리자용)
 *  - 알림 통계 조회
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final UserNotificationService userNotificationService;

    /**
     * 알림 발송 통계 조회
     */
    @GetMapping("/stats")
    public String getNotificationStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // TODO: 기간별 알림 발송 통계 조회
        return userNotificationService.getNotificationStats(startDate, endDate);
    }
}
