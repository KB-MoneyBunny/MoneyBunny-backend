package org.scoula.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.security.account.domain.AuthVO;
import org.scoula.security.account.domain.MemberVO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
  MemberVO get(String username);                    // 회원 조회 (권한 포함)
  MemberVO findByUsername(String username);         // ID 중복 체크용 조회
  int insert(MemberVO member);                      // 회원정보 저장
  //  int insertAuth(AuthVO auth);                      // 권한정보 저장
//  MemberVO findByLoginId(String loginId);   // 로그인ID로 회원 조회
  MemberVO findByLoginIdAndEmail(@Param("loginId") String loginId,
                                 @Param("email") String email);  // 이메일 인증용
  boolean resetPassword(@Param("loginId") String loginId, @Param("password") String password); // 비밀번호 재설정
  MemberVO findByEmail(@Param("email") String email);

}