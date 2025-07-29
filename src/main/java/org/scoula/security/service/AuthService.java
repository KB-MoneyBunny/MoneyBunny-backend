package org.scoula.security.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.dto.LoginDTO;

import java.util.Map;

public interface AuthService {

    // 로그인
    Map<String, String> login(LoginDTO loginDTO);

    // 로그아웃
    void logout(String username);

    // 비밀번호 재설정
    boolean resetPassword(String loginId, String password);

    // 이메일로 회원 조회
    MemberVO findByEmail(String email);

    // ID 중복 체크
    MemberVO findByUsername(String loginId);
}
