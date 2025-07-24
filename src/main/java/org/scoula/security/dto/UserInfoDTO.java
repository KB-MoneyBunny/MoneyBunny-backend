package org.scoula.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.security.account.domain.AuthVO;
import org.scoula.security.account.domain.MemberVO;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDTO {
    private String loginId;
    private String email;
    private Date createdAt;
//    private List<String> roles;

    public static UserInfoDTO of(MemberVO member) {
        return UserInfoDTO.builder()
                .loginId(member.getLoginId())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
//                .roles(member.getAuthList().stream()
//                        .map(AuthVO::getAuth)
//                        .toList())
                .build();
    }
}
