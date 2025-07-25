package org.scoula.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구독 상태 조회 API 응답용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionStatusResponse {
    private boolean subscribed;             // 구독 여부
    private String status;                  // 구독 상태 메시지
    private String message;                 // 추가 메시지

    /**
     * 구독 상태 기반 응답 생성
     */
    public static SubscriptionStatusResponse of(boolean subscribed) {
        return SubscriptionStatusResponse.builder()
                .subscribed(subscribed)
                .status(subscribed ? "ACTIVE" : "INACTIVE")
                .message(subscribed ? "맞춤형 알림을 받고 있습니다" : "알림을 받지 않고 있습니다")
                .build();
    }
}