package org.scoula.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.security.dto.UserInfoDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResultDTO {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String role;
}
