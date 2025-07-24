package org.scoula.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.service.PolicyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyScheduler {

    private final PolicyService policyService;

    /**
     * 매일 새벽 5시에 정책 데이터 동기화 실행
     * cron: "초 분 시 일 월 요일"
     * 0 0 5 * * * = 매일 05:00:00에 실행
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void scheduledPolicySync() {
        log.info("[정책 스케줄러] 정책 데이터 수집 시작 - {}", java.time.LocalDateTime.now());
        
        try {
            policyService.fetchAndSaveAllPolicies();
            log.info("[정책 스케줄러] 정책 데이터 수집 완료");
        } catch (Exception e) {
            log.error("[정책 스케줄러] 정책 데이터 수집 중 오류 발생", e);
        }
    }
}