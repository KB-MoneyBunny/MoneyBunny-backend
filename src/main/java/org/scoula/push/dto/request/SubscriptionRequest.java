package org.scoula.push.dto.request;

import lombok.Data;

/**
 * 클라이언트에서 전달된 알림 구독 요청 DTO
 * FCM 토큰과 알림 유형별 설정을 포함
 */
@Data
public class SubscriptionRequest {
    private String token; // FCM 토큰
    
    // 알림 유형별 활성화 설정 (null이면 기본값 적용)
    private Boolean isActiveBookmark;   // 북마크 알림 (기본값: false)
    private Boolean isActiveTop3;       // Top3 알림 (기본값: false)
    private Boolean isActiveNewPolicy;  // 신규 정책 알림 (기본값: false)
    private Boolean isActiveFeedback;   // 피드백 알림 (기본값: false)
    
    /**
     * 기본값 적용된 북마크 알림 설정값 반환
     */
    public boolean getBookmarkSetting() {
        return isActiveBookmark != null ? isActiveBookmark : false;
    }
    
    /**
     * 기본값 적용된 Top3 알림 설정값 반환
     */
    public boolean getTop3Setting() {
        return isActiveTop3 != null ? isActiveTop3 : false;
    }
    
    /**
     * 기본값 적용된 신규 정책 알림 설정값 반환
     */
    public boolean getNewPolicySetting() {
        return isActiveNewPolicy != null ? isActiveNewPolicy : false;
    }
    
    /**
     * 기본값 적용된 피드백 알림 설정값 반환
     */
    public boolean getFeedbackSetting() {
        return isActiveFeedback != null ? isActiveFeedback : false;
    }
}