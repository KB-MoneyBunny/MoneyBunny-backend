package org.scoula.member.dto;

import lombok.Data;

@Data
public class EmailVerifyDTO {
    private String email;
    private String code;
}
