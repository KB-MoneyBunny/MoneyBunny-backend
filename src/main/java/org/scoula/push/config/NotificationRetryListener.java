package org.scoula.push.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.service.core.AsyncNotificationService;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * FCM 발송 재시도 과정을 추적하는 리스너
 * @Retryable 메서드의 재시도 시도를 로깅
 */
@Slf4j
@Component("notificationRetryListener")
@RequiredArgsConstructor
public class NotificationRetryListener extends RetryListenerSupport {

    /**
     * 재시도 시작 시 호출
     */
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        log.debug("[FCM 재시도] 재시도 프로세스 시작");
        context.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    /**
     * 각 재시도 시도 시 호출
     */
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int retryCount = context.getRetryCount();
        long startTime = (Long) context.getAttribute("startTime");
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        log.warn("[FCM 재시도] {}차 시도 실패 - 경과시간: {}ms, 오류: {}", 
                retryCount, elapsedTime, throwable.getMessage());
                
        // Context에 재시도 정보 저장 (나중에 활용 가능)
        context.setAttribute("lastRetryCount", retryCount);
        context.setAttribute("lastError", throwable.getMessage());
        
        // ThreadLocal에 재시도 횟수 업데이트
        updateRetryCount(retryCount);
    }
    
    /**
     * ThreadLocal에 재시도 횟수 업데이트
     */
    private void updateRetryCount(int count) {
        try {
            Field field = AsyncNotificationService.class.getDeclaredField("retryCountHolder");
            field.setAccessible(true);
            ThreadLocal<Integer> retryCountHolder = (ThreadLocal<Integer>) field.get(null);
            retryCountHolder.set(count);
        } catch (Exception e) {
            log.error("재시도 횟수 업데이트 실패", e);
        }
    }

    /**
     * 재시도 완료 시 호출 (성공 또는 최종 실패)
     */
    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int finalRetryCount = context.getRetryCount();
        long startTime = (Long) context.getAttribute("startTime");
        long totalElapsedTime = System.currentTimeMillis() - startTime;
        
        if (throwable == null) {
            log.info("[FCM 재시도] 성공 - 총 시도 횟수: {}, 총 소요시간: {}ms", 
                    finalRetryCount, totalElapsedTime);
        } else {
            log.error("[FCM 재시도] 최종 실패 - 총 시도 횟수: {}, 총 소요시간: {}ms, 최종 오류: {}", 
                    finalRetryCount, totalElapsedTime, throwable.getMessage());
        }
    }
}