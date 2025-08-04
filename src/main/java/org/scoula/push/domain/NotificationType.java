package org.scoula.push.domain;

/**
 * 맞춤형 알림 유형을 정의하는 Enum
 */
public enum NotificationType {
    BOOKMARK("북마크 알림", "북마크한 정책의 오픈/마감 알림"),
    TOP3("Top3 알림", "사용자 맞춤 Top3 정책 추천"),
    NEW_POLICY("신규 정책 알림", "사용자 조건에 부합하는 신규 정책 알림"),
    FEEDBACK("피드백 알림", "개인 소비패턴 기반 맞춤형 피드백");

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
