package org.scoula.push.dto.request;

import lombok.Data;

/**
 * 클라이언트에서 전달된 FCM 토큰을 담는 DTO
 */
@Data
public class SubscriptionRequest {
    private String token; // FCM 토큰
}