package org.scoula.push.dto.request;

import lombok.Data;

/**
 * 개별 알림 타입 토글 요청 DTO
 * 경로별로 알림 타입이 구분되므로 enabled 상태만 전달
 */
@Data
public class NotificationToggleRequest {
    private String token; // FCM 토큰 (필수)
    private boolean enabled; // 활성화 여부
}