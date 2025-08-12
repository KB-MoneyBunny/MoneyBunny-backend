package org.scoula.policyInteraction.exception;

/**
 * 리뷰 작성/수정 시 발생하는 예외
 */
public class ReviewException extends RuntimeException {
    
    private final String errorCode;
    
    public ReviewException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    // 편의 메서드들
    public static ReviewException profanityDetected() {
        return new ReviewException(
            "부적절한 표현이 포함되어 있습니다.", 
            "PROFANITY_DETECTED"
        );
    }
    
    public static ReviewException notApplied() {
        return new ReviewException(
            "신청을 완료한 후 리뷰를 작성할 수 있습니다.", 
            "NOT_APPLIED"
        );
    }
    
    public static ReviewException alreadyReviewed() {
        return new ReviewException(
            "이미 리뷰를 작성하셨습니다.", 
            "ALREADY_REVIEWED"
        );
    }
    
    public static ReviewException invalidBenefitStatus() {
        return new ReviewException(
            "잘못된 혜택 상태입니다.", 
            "INVALID_BENEFIT_STATUS"
        );
    }
    
    public static ReviewException reviewNotFound() {
        return new ReviewException(
            "수정할 리뷰를 찾을 수 없습니다.", 
            "REVIEW_NOT_FOUND"
        );
    }
}