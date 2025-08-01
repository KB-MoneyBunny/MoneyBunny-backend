package org.scoula.push.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * user_notification 테이블과 매핑되는 엔티티
 * 사용자별 맞춤 알림 데이터를 관리
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationVO {
    private Long id;                 // 알림 고유 식별자
    private Long userId;            // 사용자 아이디
    private String title;           // 알림 제목
    private String message;         // 알림 본문
    private NotificationType type;  // 알림 유형 (ENUM)
    private String targetUrl;       // 이동할 URL
    private Boolean isRead;         // 읽음 여부
    private LocalDateTime createdAt; // 생성일시

    /**
     * 알림이 읽힌 상태인지 확인
     */
    public boolean isReadStatus() {
        return isRead != null && isRead;
    }

    /**
     * 읽지 않은 상태인지 확인
     */
    public boolean isUnread() {
        return !isReadStatus();
    }

    /**
     * 알림을 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
        // readAt 필드가 없다면 시간 저장은 생략하거나 DB 트리거 등으로 처리
    }

    /**
     * 북마크 알림인지 확인
     */
    public boolean isBookmarkNotification() {
        return NotificationType.BOOKMARK.equals(this.type);
    }

    /**
     * Top3 알림인지 확인
     */
    public boolean isTop3Notification() {
        return NotificationType.TOP3.equals(this.type);
    }

    /**
     * 신규 정책 알림인지 확인
     */
    public boolean isNewPolicyNotification() {
        return NotificationType.NEW_POLICY.equals(this.type);
    }

    /**
     * 피드백 알림인지 확인
     */
    public boolean isFeedbackNotification() {
        return NotificationType.FEEDBACK.equals(this.type);
    }

    /**
     * 알림이 유효한지 확인 (필수 필드 검증)
     */
    public boolean isValid() {
        return userId != null &&
                title != null && !title.trim().isEmpty() &&
                message != null && !message.trim().isEmpty() &&
                type != null;
    }

    /**
     * 디스플레이용 타입 이름 반환
     */
    public String getDisplayTypeName() {
        return type != null ? type.getDisplayName() : "알 수 없음";
    }
}
