package org.scoula.push.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSendLogVO {

    private Long id;                   // 기본 키
    private Long notificationId;       // 알림 아이디 (user_notification.id 참조)
    private String fcmToken;           // 전송 대상 FCM 토큰
    private SendStatus sendStatus;     // 발송 상태: PENDING / SUCCESS / FAILED
    private int attemptCount;          // 시도 횟수
    private String errorMessage;       // 오류 메시지 (있을 경우)
    private LocalDateTime sentAt;      // 발송 시간
    private LocalDateTime createdAt;   // 생성 시간

    public enum SendStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}