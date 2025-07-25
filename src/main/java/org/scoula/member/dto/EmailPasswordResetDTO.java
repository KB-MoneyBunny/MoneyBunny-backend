package org.scoula.member.dto;

import lombok.Data;

@Data
public class EmailPasswordResetDTO {
    private String loginId;
    private String email;
}
