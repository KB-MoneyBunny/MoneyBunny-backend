package org.scoula.codef.dto;

import lombok.Data;

// 1. DTO 클래스: 사용자 입력값 받기
@Data
public class AccountConnectRequest {
    private String organization;  // 은행 코드 (예: "0020")
    private String loginId;       // 사용자 아이디
    private String password;      // 사용자 비밀번호
}
