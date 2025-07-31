package org.scoula.member.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.security.account.domain.MemberVO;

import java.util.Optional;

public interface MemberService {
  boolean checkDuplicate(String loginId);       // username → loginId
  MemberDTO get(String loginId);
  MemberDTO join(MemberJoinDTO member);
  MemberVO findByUsername(String loginId); // 로그인 ID로 회원 조회
  void validateJoinInfo(MemberJoinDTO dto); // 회원가입 관련 유효성 검사
  boolean isEmailExists(String email); // 가입된 이메일 존재 여부


}
