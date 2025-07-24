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
public class UserNotification {
    private Long id;                        // 알림 고유 식별자
    private Long userId;                    // 사용자 아이디
    private String title;                   // 알림 제목
    private String message;                 // 알림 본문
    private String type;                    // 알림 유형 (POLICY, FEEDBACK, SYSTEM)
    private String targetUrl;               // 이동할 URL (선택적)
    private Boolean isRead;                 // 읽음 여부
    private LocalDateTime createdAt;        // 생성시간
    private LocalDateTime readAt;           // 읽은 시간 (선택적)

    /**
     * 알림 타입을 NotificationType enum으로 반환
     */
    public NotificationType getNotificationType() {
        return NotificationType.fromString(this.type);
    }

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
        this.readAt = LocalDateTime.now();
    }

    /**
     * 정책 알림인지 확인
     */
    public boolean isPolicyNotification() {
        return NotificationType.POLICY.name().equals(this.type);
    }

    /**
     * 피드백 알림인지 확인
     */
    public boolean isFeedbackNotification() {
        return NotificationType.FEEDBACK.name().equals(this.type);
    }

    /**
     * 시스템 알림인지 확인
     */
    public boolean isSystemNotification() {
        return NotificationType.SYSTEM.name().equals(this.type);
    }

    /**
     * 알림이 유효한지 확인 (필수 필드 검증)
     */
    public boolean isValid() {
        return userId != null && 
               title != null && !title.trim().isEmpty() &&
               message != null && !message.trim().isEmpty() &&
               NotificationType.isValidType(this.type);
    }

    /**
     * 디스플레이용 타입 이름 반환
     */
    public String getDisplayTypeName() {
        NotificationType notificationType = getNotificationType();
        return notificationType != null ? notificationType.getDisplayName() : "알 수 없음";
    }
}