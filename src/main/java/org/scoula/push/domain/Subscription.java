package org.scoula.push.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * subscription 테이블과 매핑되는 엔티티
 */
@Data
public class Subscription {
    private Long id;
    private Long userId;
    private String endpoint; // FCM 토큰
    private Boolean isActiveBookmark;   // 북마크 알림 활성화 여부
    private Boolean isActiveTop3;       // Top3 알림 활성화 여부
    private Boolean isActiveNewPolicy;  // 신규 정책 알림 활성화 여부
    private Boolean isActiveFeedback;   // 피드백 알림 활성화 여부
    private LocalDateTime createdAt;
    
    /**
     * 전체 알림이 활성화되어 있는지 확인
     * (하나라도 활성화되어 있으면 true)
     */
    public boolean hasAnyActiveNotification() {
        return (isActiveBookmark != null && isActiveBookmark) ||
               (isActiveTop3 != null && isActiveTop3) ||
               (isActiveNewPolicy != null && isActiveNewPolicy) ||
               (isActiveFeedback != null && isActiveFeedback);
    }
    
    /**
     * 특정 알림 타입이 활성화되어 있는지 확인
     */
    public boolean isNotificationTypeActive(org.scoula.push.domain.NotificationType type) {
        return switch (type) {
            case BOOKMARK -> isActiveBookmark != null && isActiveBookmark;
            case TOP3 -> isActiveTop3 != null && isActiveTop3;
            case NEW_POLICY -> isActiveNewPolicy != null && isActiveNewPolicy;
            case FEEDBACK -> isActiveFeedback != null && isActiveFeedback;
        };
    }
}