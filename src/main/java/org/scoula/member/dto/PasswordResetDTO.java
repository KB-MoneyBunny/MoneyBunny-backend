package org.scoula.member.dto;

import lombok.Data;

@Data
public class PasswordResetDTO {
    private String loginId;
    private String password;
}

