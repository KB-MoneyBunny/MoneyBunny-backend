package org.scoula.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.service.PolicyService;
import org.scoula.policyInteraction.service.UserVectorBatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyScheduler {

    private final PolicyService policyService;
    private final UserVectorBatchService userVectorBatchService;

    /**
     * 매일 새벽 5시에 정책 데이터 동기화 및 사용자 벡터 갱신 실행
     * cron: "초 분 시 일 월 요일"
     * 0 0 5 * * * = 매일 05:00:00에 실행
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void scheduledPolicySync() {
        log.info("[정책 스케줄러] 배치 작업 시작 - {}", java.time.LocalDateTime.now());
        
        try {
            // 1. 정책 데이터 수집
            log.info("[정책 스케줄러] 정책 데이터 수집 시작");
            policyService.fetchAndSaveAllPolicies();
            log.info("[정책 스케줄러] 정책 데이터 수집 완료");
            
            // 2. 사용자 벡터의 3차원 전체 EMA 갱신 (전날 조회 데이터 기반)
            log.info("[정책 스케줄러] 사용자 벡터 3차원 갱신 시작");
            userVectorBatchService.updateUserVectorsByEMA();
            log.info("[정책 스케줄러] 사용자 벡터 3차원 갱신 완료");
            
            log.info("[정책 스케줄러] 모든 배치 작업 완료");
        } catch (Exception e) {
            log.error("[정책 스케줄러] 배치 작업 중 오류 발생", e);
        }
    }
}