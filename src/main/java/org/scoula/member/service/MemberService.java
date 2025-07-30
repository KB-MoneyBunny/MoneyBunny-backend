package org.scoula.member.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.security.account.domain.MemberVO;

import java.util.Optional;

public interface MemberService {
  boolean checkDuplicate(String loginId);       // username → loginId
  MemberDTO get(String loginId);
  MemberDTO join(MemberJoinDTO member);
  // 로그인 ID로 회원 조회
  MemberVO findByUsername(String loginId);

}
