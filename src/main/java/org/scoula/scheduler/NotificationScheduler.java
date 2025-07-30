package org.scoula.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.service.BookmarkPolicyNotificationService;
import org.scoula.push.service.UserNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 알림 스케줄러
 * - 북마크 기반 정책 알림 자동 발송
 * - 소비패턴 피드백 알림 자동 발송  
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final UserNotificationService userNotificationService;
    private final BookmarkPolicyNotificationService bookmarkPolicyNotificationService;

    /**
     * 북마크 기반 정책 알림 스케줄러 - 매일 오전 9시 실행
     */
    @Scheduled(cron = "0 0 12 * * *")
    public void scheduledPolicyNotifications() {
        log.info("📅 [정책 알림 스케줄러] 시작");
        
        try {
            bookmarkPolicyNotificationService.checkAndSendBookmarkNotifications();
            log.info("📅 [정책 알림 스케줄러] 완료");
        } catch (Exception e) {
            log.error("📅 [정책 알림 스케줄러] 오류: {}", e.getMessage());
        }
    }

    /**
     * 피드백 알림 스케줄러 - 매주 일요일 저녁 8시 실행
     */
    @Scheduled(cron = "0 0 20 * * SUN")
    public void scheduledFeedbackNotifications() {
        log.info("📅 [피드백 알림 스케줄러] 시작");
        
        try {
            userNotificationService.triggerBatchPersonalizedFeedback();
            log.info("📅 [피드백 알림 스케줄러] 완료");
        } catch (Exception e) {
            log.error("📅 [피드백 알림 스케줄러] 오류: {}", e.getMessage());
        }
    }
}