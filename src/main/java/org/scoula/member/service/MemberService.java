package org.scoula.member.service;

import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;

public interface MemberService {
  boolean checkDuplicate(String username);     // ID 중복 체크
  MemberDTO get(String username);              // 회원 조회
  MemberDTO join(MemberJoinDTO member);        // 회원가입
}