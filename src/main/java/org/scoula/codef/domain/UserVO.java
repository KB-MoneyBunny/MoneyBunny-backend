package org.scoula.codef.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO {

    private Long userId;
    private String loginId;
    private String email;
    private String password;
    private String name;
    private java.util.Date createAt;
    private Integer point;

    private List<UserCardVO> cards;
    private List<UserAccountVO> accounts;
}