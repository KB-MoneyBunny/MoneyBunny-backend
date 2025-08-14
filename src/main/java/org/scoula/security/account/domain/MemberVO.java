package org.scoula.security.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberVO {
    private Long userId;
    private String loginId;
    private String email;
    private String password;
    private Date createdAt;              // ← 변경됨 (regDate → createdAt)
    private int point;
    private String name;
//    private List<AuthVO> authList;
    private int profileImageId;
}
