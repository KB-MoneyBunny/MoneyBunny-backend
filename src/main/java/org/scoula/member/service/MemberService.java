package org.scoula.member.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.security.account.domain.MemberVO;

import java.util.Optional;

public interface MemberService {
  boolean checkDuplicate(String loginId);       // username → loginId
  MemberDTO get(String loginId);
  MemberDTO join(MemberJoinDTO member);
  Optional<MemberDTO> login(String username, String password);
  MemberVO findByUsername(String loginId);
  boolean resetPassword(String loginId, String password);
  MemberVO findByEmail(String email); // ID 찾기

}
