package org.scoula.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 스케줄러 상태 조회 API 응답용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerStatusResponse {
    private String type;                    // 스케줄러 타입 (POLICY, FEEDBACK)
    private String status;                  // 상태 (ACTIVE, INACTIVE)
    private String description;             // 설명
    private String schedule;                // 실행 스케줄 정보

    /**
     * 정책 알림 스케줄러 상태 응답 생성
     */
    public static SchedulerStatusResponse forPolicy(String status) {
        return SchedulerStatusResponse.builder()
                .type("POLICY")
                .status(status)
                .description("북마크한 정책의 오픈/마감 알림")
                .schedule("매일 오전 9시 실행")
                .build();
    }

    /**
     * 피드백 알림 스케줄러 상태 응답 생성
     */
    public static SchedulerStatusResponse forFeedback(String status) {
        return SchedulerStatusResponse.builder()
                .type("FEEDBACK")
                .status(status)
                .description("개인 소비패턴 기반 맞춤형 피드백")
                .schedule("매주 일요일 저녁 8시 실행")
                .build();
    }
}