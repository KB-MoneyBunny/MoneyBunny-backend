package org.scoula.push.domain;

/**
 * 맞춤형 알림 유형을 정의하는 Enum
 */
public enum NotificationType {
    POLICY("정책 알림", "북마크한 정책의 오픈/마감 알림"),           
    FEEDBACK("피드백 알림", "개인 소비패턴 기반 맞춤형 피드백"),
    SYSTEM("시스템 알림", "앱 업데이트, 점검 등 시스템 관련 알림");       

    private final String displayName;
    private final String description;

    NotificationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 문자열로부터 NotificationType을 찾는 메서드
     */
    public static NotificationType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 유효한 타입인지 확인하는 메서드
     */
    public static boolean isValidType(String type) {
        return fromString(type) != null;
    }
}