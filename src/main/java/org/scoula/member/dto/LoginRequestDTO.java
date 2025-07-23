package org.scoula.member.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String loginId;
    private String password;
}
