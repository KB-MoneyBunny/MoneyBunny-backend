package org.scoula.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.push.domain.Subscription;

/**
 * 구독 상태 조회 API 응답용 DTO
 * 알림 유형별 구독 상태를 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionStatusResponse {
    private boolean subscribed;             // 전체 구독 여부 (하나라도 활성화되어 있으면 true)
    private String status;                  // 구독 상태 메시지
    private String message;                 // 추가 메시지
    
    // 알림 유형별 구독 상태
    private boolean isActiveBookmark;       // 북마크 알림 상태
    private boolean isActiveTop3;           // Top3 알림 상태  
    private boolean isActiveNewPolicy;      // 신규 정책 알림 상태
    private boolean isActiveFeedback;       // 피드백 알림 상태

    /**
     * Subscription 엔티티로부터 응답 생성
     */
    public static SubscriptionStatusResponse from(Subscription subscription) {
        if (subscription == null) {
            return SubscriptionStatusResponse.builder()
                    .subscribed(false)
                    .status("INACTIVE")
                    .message("알림 구독이 되어있지 않습니다")
                    .isActiveBookmark(false)
                    .isActiveTop3(false)
                    .isActiveNewPolicy(false)
                    .isActiveFeedback(false)
                    .build();
        }
        
        boolean hasAnyActive = subscription.hasAnyActiveNotification();
        
        return SubscriptionStatusResponse.builder()
                .subscribed(hasAnyActive)
                .status(hasAnyActive ? "ACTIVE" : "INACTIVE")
                .message(hasAnyActive ? "맞춤형 알림을 받고 있습니다" : "모든 알림이 비활성화되어 있습니다")
                .isActiveBookmark(subscription.getIsActiveBookmark() != null ? subscription.getIsActiveBookmark() : false)
                .isActiveTop3(subscription.getIsActiveTop3() != null ? subscription.getIsActiveTop3() : false)
                .isActiveNewPolicy(subscription.getIsActiveNewPolicy() != null ? subscription.getIsActiveNewPolicy() : false)
                .isActiveFeedback(subscription.getIsActiveFeedback() != null ? subscription.getIsActiveFeedback() : false)
                .build();
    }
}