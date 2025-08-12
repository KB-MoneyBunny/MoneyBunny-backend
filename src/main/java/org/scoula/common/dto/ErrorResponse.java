package org.scoula.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 에러 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String message;  // 사용자에게 보여줄 에러 메시지
    private String code;     // 에러 코드 (프론트엔드에서 분기 처리용)
    private Long timestamp;  // 에러 발생 시간
    
    public static ErrorResponse of(String message, String code) {
        return ErrorResponse.builder()
                .message(message)
                .code(code)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static ErrorResponse of(String message) {
        return ErrorResponse.builder()
                .message(message)
                .code("ERROR")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}