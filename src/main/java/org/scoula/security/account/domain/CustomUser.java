package org.scoula.security.account.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;


@Getter
@Setter
public class CustomUser extends User {

    private MemberVO member; // 실제 사용자 데이터를 담는 객체


    // User 클래스 기본 생성자를 그대로 호출
    public CustomUser(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }


    // MemberVO를 받는 생성자 - 실제로 주로 사용
    public CustomUser(MemberVO vo) {
        super(vo.getLoginId(), vo.getPassword(), List.of());  // 권한 없음
        this.member = vo;
    }

}
