package org.scoula.push.dto.request;

import lombok.Data;

/**
 * 클라이언트에서 전달된 알림 구독 요청 DTO
 * FCM 토큰과 알림 유형별 설정을 포함
 */
@Data
public class SubscriptionRequest {
    private String token; // FCM 토큰
    
    // 알림 유형별 활성화 설정 (기본값: false)
    private boolean isActiveBookmark;   // 북마크 알림
    private boolean isActiveTop3;       // Top3 알림
    private boolean isActiveNewPolicy;  // 신규 정책 알림
    private boolean isActiveFeedback;   // 피드백 알림

}