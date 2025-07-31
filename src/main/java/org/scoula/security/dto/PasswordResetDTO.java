package org.scoula.security.dto;

import lombok.Data;

@Data
public class PasswordResetDTO {
    private String loginId;
    private String password;
}

