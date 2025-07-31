package org.scoula.codef.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryUtil {
    // 리턴값 필요하면 이거
    public static <T> T retry(int maxAttempts, long delayMillis, java.util.concurrent.Callable<T> action) throws Exception {
        int attempt = 0;
        Exception last = null;
        while (attempt < maxAttempts) {
            try {
                return action.call();
            } catch (Exception e) {
                last = e;
                attempt++;
                if (attempt < maxAttempts) {
                    log.warn("[재시도] {}회 실패, {}ms 후 재시도: {}", attempt, delayMillis * attempt, e.getMessage());
                    Thread.sleep(delayMillis * attempt); // 점진적 대기
                }
            }
        }
        log.error("[재시도] {}회 모두 실패! 마지막 에러: {}", maxAttempts, last.getMessage());
        throw last;
    }

    // void 함수(리턴값 없는 함수)
    public static void retryVoid(int maxAttempts, long delayMillis, RunnableWithException action) throws Exception {
        int attempt = 0;
        Exception last = null;
        while (attempt < maxAttempts) {
            try {
                action.run();
                return; // 성공하면 종료
            } catch (Exception e) {
                last = e;
                attempt++;
                if (attempt < maxAttempts) {
                    log.warn("[재시도] {}회 실패, {}ms 후 재시도: {}", attempt, delayMillis * attempt, e.getMessage());
                    Thread.sleep(delayMillis * attempt);
                }
            }
        }
        log.error("[재시도] {}회 모두 실패! 마지막 에러: {}", maxAttempts, last.getMessage());
        throw last;
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}