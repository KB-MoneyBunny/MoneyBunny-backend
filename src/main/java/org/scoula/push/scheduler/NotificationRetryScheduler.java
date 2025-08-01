package org.scoula.push.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.domain.NotificationSendLogVO;
import org.scoula.push.domain.UserNotificationVO;
import org.scoula.push.mapper.NotificationSendLogMapper;
import org.scoula.push.mapper.UserNotificationMapper;
import org.scoula.push.service.AsyncNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 누락된 알림 재전송 스케줄러
 * PENDING 상태인 발송 로그를 주기적으로 체크하여 재전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationSendLogMapper sendLogMapper;
    private final UserNotificationMapper userNotificationMapper;
    private final AsyncNotificationService asyncNotificationService;

    /**
     * 북마크 알림 1차 재시도 - 12:20
     */
    @Scheduled(cron = "0 20 12 * * *", zone = "Asia/Seoul")
    public void firstRetry() {
        log.info("📧 [북마크 알림] 1차 재시도 체크 (12:20)");
        retryFailedNotifications();
    }

    /**
     * 북마크 알림 2차 재시도 - 12:40
     */
    @Scheduled(cron = "0 40 12 * * *", zone = "Asia/Seoul")
    public void secondRetry() {
        log.info("📧 [북마크 알림] 2차 재시도 체크 (12:40)");
        retryFailedNotifications();
    }

    /**
     * 북마크 알림 3차 재시도 - 13:00 (최종)
     */
    @Scheduled(cron = "0 0 13 * * *", zone = "Asia/Seoul")
    public void finalRetry() {
        log.info("📧 [북마크 알림] 3차 재시도 체크 (13:00) - 최종");
        retryFailedNotifications();
    }

    /**
     * 누락된 알림 재전송 처리
     * 10분 이상 PENDING 상태인 로그를 대상으로 재전송 시도
     */
    private void retryFailedNotifications() {
        log.info("📧 [알림 재전송] 누락 알림 체크 시작");
        
        try {
            // 10분 이상 PENDING 상태인 로그 조회
            List<NotificationSendLogVO> pendingLogs = sendLogMapper.findPendingLogsByTime(10);
            
            if (pendingLogs.isEmpty()) {
                log.debug("📧 [알림 재전송] 재전송 대상 없음");
                return;
            }
            
            log.info("📧 [알림 재전송] 재전송 대상 {}건 발견", pendingLogs.size());
            
            int retryCount = 0;
            for (NotificationSendLogVO sendLog : pendingLogs) {
                try {
                    // 최대 재시도 횟수 체크
                    if (sendLog.getAttemptCount() >= 3) {
                        log.warn("📧 [알림 재전송] 최대 재시도 횟수 초과 - 로그 ID: {}", sendLog.getId());
                        continue;
                    }
                    
                    // 원본 알림 정보 조회
                    UserNotificationVO notification = findNotificationById(sendLog.getNotificationId());
                    if (notification == null) {
                        log.error("📧 [알림 재전송] 원본 알림을 찾을 수 없음 - 알림 ID: {}", sendLog.getNotificationId());
                        continue;
                    }
                    
                    // 재전송 시도
                    asyncNotificationService.retrySendFCM(sendLog, notification.getTitle(), notification.getMessage());
                    retryCount++;
                    
                    log.debug("📧 [알림 재전송] 재전송 시작 - 로그 ID: {}, 시도 횟수: {}", 
                            sendLog.getId(), sendLog.getAttemptCount() + 1);
                    
                } catch (Exception e) {
                    log.error("📧 [알림 재전송] 재전송 처리 중 오류 - 로그 ID: {}, 오류: {}", 
                            sendLog.getId(), e.getMessage());
                }
            }
            
            log.info("📧 [알림 재전송] 완료 - 재전송 시도: {}건", retryCount);
            
        } catch (Exception e) {
            log.error("📧 [알림 재전송] 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 알림 ID로 원본 알림 조회
     */
    private UserNotificationVO findNotificationById(Long notificationId) {
        try {
            return userNotificationMapper.findById(notificationId);
        } catch (Exception e) {
            log.error("알림 조회 중 오류 - 알림 ID: {}", notificationId, e);
            return null;
        }
    }
}