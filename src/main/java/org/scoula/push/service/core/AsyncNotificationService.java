package org.scoula.push.service.core;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.domain.NotificationSendLogVO;
import org.scoula.push.mapper.NotificationSendLogMapper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 비동기 FCM 전송 서비스
 * 발송 로그 기록 및 재시도 메커니즘 포함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationSendLogMapper sendLogMapper;
    
    // 재시도 횟수를 추적하기 위한 ThreadLocal
    private static final ThreadLocal<Integer> retryCountHolder = new ThreadLocal<>();

    /**
     * 비동기 FCM 발송 (발송 로그 자동 기록)
     */
    @Async("fcmTaskExecutor")
    public CompletableFuture<Void> sendFCMWithLogging(Long notificationId, String fcmToken, String title, String message) {
        // 1. 발송 로그 생성 (PENDING 상태)
        NotificationSendLogVO sendLog = NotificationSendLogVO.builder()
                .notificationId(notificationId)
                .fcmToken(fcmToken)
                .sendStatus(NotificationSendLogVO.SendStatus.PENDING)
                .attemptCount(1)
                .createdAt(LocalDateTime.now())
                .build();

        sendLogMapper.insertSendLog(sendLog);
        Long logId = sendLog.getId();

        // 2. FCM 발송 시도
        int actualAttempts = 1; // 기본값
        try {
            retryCountHolder.set(1); // 초기화
            sendFCMWithRetry(fcmToken, title, message);
            
            // 실제 시도 횟수 가져오기
            actualAttempts = retryCountHolder.get();
            
            // 성공 시 로그 업데이트 (실제 시도 횟수 포함)
            sendLogMapper.updateSendLogStatus(logId, 
                NotificationSendLogVO.SendStatus.SUCCESS, 
                null, 
                LocalDateTime.now());
            
            // 시도 횟수 업데이트
            if (actualAttempts > 1) {
                sendLogMapper.incrementAttemptCount(logId); // 실제 재시도 횟수만큼 증가
                for (int i = 2; i < actualAttempts; i++) {
                    sendLogMapper.incrementAttemptCount(logId);
                }
            }
                
            log.info("[FCM 발송 성공] 알림 ID: {}, 로그 ID: {}, 시도 횟수: {}", 
                    notificationId, logId, actualAttempts);
            
        } catch (Exception e) {
            // 실제 시도 횟수 가져오기
            actualAttempts = retryCountHolder.get() != null ? retryCountHolder.get() : 3;
            
            // 실패 시 로그 업데이트 (실제 시도 횟수 포함)
            sendLogMapper.updateSendLogStatus(logId, 
                NotificationSendLogVO.SendStatus.FAILED, 
                e.getMessage(), 
                LocalDateTime.now());
            
            // 시도 횟수 업데이트
            for (int i = 1; i < actualAttempts; i++) {
                sendLogMapper.incrementAttemptCount(logId);
            }
                
            log.error("[FCM 발송 실패] 알림 ID: {}, 로그 ID: {}, 시도 횟수: {}, 오류: {}", 
                    notificationId, logId, actualAttempts, e.getMessage());
        } finally {
            retryCountHolder.remove(); // 메모리 누수 방지
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * FCM 발송 (@Retryable 재시도 메커니즘 포함)
     * - 1차: 즉시
     * - 2차: 3초 후
     * - 3차: 9초 후 (최대 15초까지)
     */
    @Retryable(
        value = {FirebaseMessagingException.class, Exception.class},
        maxAttempts = 3, 
        backoff = @Backoff(delay = 3000, multiplier = 3, maxDelay = 15000),
        listeners = {"notificationRetryListener"}
    )
    private void sendFCMWithRetry(String fcmToken, String title, String message) throws FirebaseMessagingException {
        Message fcmMessage = Message.builder()
                .setToken(fcmToken)
                .putData("title", title)
                .putData("body", message)
                .build();

        String response = firebaseMessaging.send(fcmMessage);
        log.debug("[FCM 발송] 응답: {}", response);
    }

    /**
     * 재전송을 위한 FCM 발송 (기존 로그 ID 사용)
     */
    @Async("fcmTaskExecutor")
    public CompletableFuture<Void> retrySendFCM(NotificationSendLogVO sendLog, String title, String message) {
        Long logId = sendLog.getId();
        
        try {
            // 재시도 횟수 증가
            sendLogMapper.incrementAttemptCount(logId);
            
            // FCM 발송
            sendFCMWithRetry(sendLog.getFcmToken(), title, message);
            
            // 성공 시 로그 업데이트
            sendLogMapper.updateSendLogStatus(logId, 
                NotificationSendLogVO.SendStatus.SUCCESS, 
                null, 
                LocalDateTime.now());
                
            log.info("[FCM 재전송 성공] 로그 ID: {}, 시도 횟수: {}", logId, sendLog.getAttemptCount() + 1);
            
        } catch (Exception e) {
            // 실패 시 로그 업데이트 (최대 3회까지만 재시도)
            if (sendLog.getAttemptCount() >= 3) {
                sendLogMapper.updateSendLogStatus(logId, 
                    NotificationSendLogVO.SendStatus.FAILED, 
                    "최대 재시도 횟수 초과: " + e.getMessage(), 
                    LocalDateTime.now());
                    
                log.error("[FCM 재전송 최종 실패] 로그 ID: {}, 최종 시도 횟수: {}", logId, sendLog.getAttemptCount() + 1);
            } else {
                log.warn("[FCM 재전송 실패] 로그 ID: {}, 시도 횟수: {}, 오류: {}", logId, sendLog.getAttemptCount() + 1, e.getMessage());
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}