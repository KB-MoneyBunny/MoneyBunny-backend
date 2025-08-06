package org.scoula.push.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.push.mapper.NotificationSendLogMapper;
import org.scoula.push.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FCM 토큰 정리 서비스
 * - notification_send_log 테이블에서 만료된 토큰 감지
 * - 동일한 토큰으로 여러 번 실패한 경우 subscription 테이블에서 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final NotificationSendLogMapper notificationSendLogMapper;
    private final SubscriptionMapper subscriptionMapper;

    // 정리 대상 에러 메시지
    private static final String TARGET_ERROR_MESSAGE = "Requested entity was not found.";
    
    // 최소 실패 횟수 (동일 토큰으로 몇 번 실패했는지)
    private static final int MIN_FAILURE_COUNT = 3;
    
    // 최근 시간 (hours) - 너무 오래된 로그는 제외 (30일)
    private static final int RECENT_HOURS = 720;

    /**
     * 만료된 FCM 토큰 정리
     * - 최근 30일 내 동일한 토큰으로 "Requested entity was not found" 에러가 3회 이상 발생한 경우
     * - 해당 토큰을 subscription 테이블에서 삭제
     */
    @Transactional
    public void cleanupInvalidTokens() {
        log.info("🧹 [토큰 정리] 만료된 FCM 토큰 정리 작업 시작");
        
        try {
            // 1. 최근 30일간 동일 토큰으로 여러 번 실패한 토큰들 조회
            List<String> invalidTokens = notificationSendLogMapper.findTokensByFailureCount(
                TARGET_ERROR_MESSAGE, 
                RECENT_HOURS,
                MIN_FAILURE_COUNT
            );
            
            if (invalidTokens.isEmpty()) {
                log.info("🧹 [토큰 정리] 정리할 만료된 토큰이 없습니다.");
                return;
            }
            
            log.info("🧹 [토큰 정리] {}개의 만료된 토큰 발견 (최근 30일간 {}회 이상 실패)", 
                    invalidTokens.size(), MIN_FAILURE_COUNT);
            
            // 2. 각 토큰과 관련 로그 삭제
            int deletedCount = 0;
            for (String token : invalidTokens) {
                try {
                    // 구독 정보 삭제
                    subscriptionMapper.deleteByToken(token);
                    
                    // 관련 로그도 삭제 (같은 토큰을 계속 찾지 않도록)
                    notificationSendLogMapper.deleteByToken(token);
                    
                    deletedCount++;
                    log.info("🧹 [토큰 정리] 토큰 및 관련 로그 삭제 완료: {}", 
                            token.length() > 20 ? token.substring(0, 20) + "..." : token);
                } catch (Exception e) {
                    log.error("🧹 [토큰 정리] 토큰 삭제 실패: {}, 에러: {}", 
                            token.length() > 20 ? token.substring(0, 20) + "..." : token, 
                            e.getMessage());
                }
            }
            
            log.info("🧹 [토큰 정리] 작업 완료 - 총 {}개 중 {}개 삭제", 
                    invalidTokens.size(), deletedCount);
            
        } catch (Exception e) {
            log.error("🧹 [토큰 정리] 작업 중 오류 발생", e);
        }
    }

    /**
     * 특정 토큰 즉시 삭제 (수동 실행용)
     * @param token 삭제할 FCM 토큰
     */
    @Transactional
    public void deleteSpecificToken(String token) {
        try {
            // 구독 정보 삭제
            subscriptionMapper.deleteByToken(token);
            
            // 관련 로그도 삭제
            notificationSendLogMapper.deleteByToken(token);
            
            log.info("🧹 [토큰 정리] 특정 토큰 및 관련 로그 삭제 완료: {}", 
                    token.length() > 20 ? token.substring(0, 20) + "..." : token);
        } catch (Exception e) {
            log.error("🧹 [토큰 정리] 특정 토큰 삭제 실패: {}, 에러: {}", 
                    token.length() > 20 ? token.substring(0, 20) + "..." : token, 
                    e.getMessage());
            throw e;
        }
    }
}