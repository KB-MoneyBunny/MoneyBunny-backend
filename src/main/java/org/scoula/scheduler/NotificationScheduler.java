package org.scoula.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.service.BookmarkPolicyNotificationService;
import org.scoula.push.service.NewPolicyNotificationService;
import org.scoula.push.service.UserNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 알림 스케줄러
 * - 북마크 기반 정책 알림 실시간 체크 및 발송
 * - 소비패턴 피드백 알림 자동 발송  
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final BookmarkPolicyNotificationService bookmarkPolicyNotificationService;
    private final NewPolicyNotificationService newPolicyNotificationService;
    private final UserNotificationService userNotificationService;

    /**
     * 북마크 알림 실시간 체크 및 발송 스케줄러 - 매일 오후 12시 실행
     */
    @Scheduled(cron = "0 0 12 * * *")
    public void sendBookmarkNotifications() {
        log.info("📅 [북마크 알림] 실시간 체크 및 발송 시작");
        
        try {
            bookmarkPolicyNotificationService.checkAndSendBookmarkNotifications();
            log.info("📅 [북마크 알림] 실시간 체크 및 발송 완료");
        } catch (Exception e) {
            log.error("📅 [북마크 알림] 오류: {}", e.getMessage());
        }
    }

    /**
     * 신규 정책 알림 스케줄러 - 매일 오후 6시 실행
     */
    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void sendNewPolicyNotifications() {
        log.info("📅 [신규 정책 알림] 스케줄러 시작");
        
        try {
            newPolicyNotificationService.processNewPolicyAlerts();
            log.info("📅 [신규 정책 알림] 스케줄러 완료");
        } catch (Exception e) {
            log.error("📅 [신규 정책 알림] 스케줄러 오류: {}", e.getMessage());
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