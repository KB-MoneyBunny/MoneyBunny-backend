package org.scoula.member.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;

import java.util.Optional;

public interface MemberService {
  boolean checkDuplicate(String loginId);       // username → loginId
  MemberDTO get(String loginId);
  MemberDTO join(MemberJoinDTO member);
  Optional<MemberDTO> login(String username, String password);
}
