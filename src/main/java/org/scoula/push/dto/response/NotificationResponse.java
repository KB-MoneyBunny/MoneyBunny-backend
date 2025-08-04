package org.scoula.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.push.domain.NotificationType;
import org.scoula.push.domain.UserNotificationVO;

import java.time.LocalDateTime;

/**
 * 알림 조회 API 응답용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;                        // 알림 ID
    private String title;                   // 알림 제목
    private String message;                 // 알림 본문
    private NotificationType type;          // 알림 타입 (BOOKMARK, TOP3, NEW_POLICY, FEEDBACK)
    private String typeName;                // 알림 타입 표시명
    private String targetUrl;               // 이동할 URL
    private boolean isRead;                 // 읽음 여부
    private LocalDateTime createdAt;        // 생성시간

    /**
     * UserNotificationVO 엔티티로부터 DTO 생성
     */
    public static NotificationResponse from(UserNotificationVO notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .typeName(notification.getDisplayTypeName())
                .targetUrl(notification.getTargetUrl())
                .isRead(notification.isReadStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}