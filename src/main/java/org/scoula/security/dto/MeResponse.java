package org.scoula.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class MeResponse {
    private Long id;              // MemberVO.userId
    private String username;      // MemberVO.loginId
    private String email;         // MemberVO.email
    private String name;          // MemberVO.name
    private List<String> roles;   // ["USER","ADMIN",...]
    private Integer profileImageId; // MemberVO.profileImageId
    private Date createdAt;       // MemberVO.createdAt
    private Integer point;        // MemberVO.point
}
