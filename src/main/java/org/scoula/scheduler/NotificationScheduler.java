package org.scoula.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.service.UserNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 단순한 알림 스케줄러
 * - 정책 알림 자동 발송
 * - 소비패턴 피드백 알림 자동 발송  
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final UserNotificationService userNotificationService;

    /**
     * 정책 알림 스케줄러 - 매일 오전 9시 실행
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void scheduledPolicyNotifications() {
        log.info("[정책 알림 스케줄러] 시작");
        
        try {
            // TODO: 북마크한 정책의 오픈/마감 알림 발송
            userNotificationService.triggerBatchPersonalizedFeedback();
            log.info("[정책 알림 스케줄러] 완료");
        } catch (Exception e) {
            log.error("[정책 알림 스케줄러] 오류: {}", e.getMessage());
        }
    }

    /**
     * 피드백 알림 스케줄러 - 매주 일요일 저녁 8시 실행
     */
    @Scheduled(cron = "0 0 20 * * SUN")
    public void scheduledFeedbackNotifications() {
        log.info("[피드백 알림 스케줄러] 시작");
        
        try {
            // TODO: 개인별 소비패턴 피드백 알림 발송
            userNotificationService.triggerBatchPersonalizedFeedback();
            log.info("[피드백 알림 스케줄러] 완료");
        } catch (Exception e) {
            log.error("[피드백 알림 스케줄러] 오류: {}", e.getMessage());
        }
    }
}