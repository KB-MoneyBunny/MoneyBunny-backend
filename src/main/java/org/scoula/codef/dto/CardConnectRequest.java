package org.scoula.codef.dto;

import lombok.Data;

@Data
public class CardConnectRequest {
    private String organization;  // 은행 카드 코드
    private String loginId;       // 사용자 아이디
    private String password;      // 사용자 비밀번호
}
