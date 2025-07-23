package org.scoula.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.security.dto.UserInfoDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResultDTO {
    private String token;
    private UserInfoDTO user;
}
