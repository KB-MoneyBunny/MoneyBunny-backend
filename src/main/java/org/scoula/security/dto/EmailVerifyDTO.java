package org.scoula.security.dto;

import lombok.Data;

@Data
public class EmailVerifyDTO {
    private String email;
    private String code;
}
