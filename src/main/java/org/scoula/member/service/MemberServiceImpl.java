package org.scoula.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.security.account.domain.MemberVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final PasswordEncoder passwordEncoder;
  private final MemberMapper mapper;

  // ID 중복 체크
  @Override
  public boolean checkDuplicate(String username) {
    MemberVO member = mapper.findByUsername(username);
    return member != null;
  }

  // 회원 정보 조회
  @Override
  public MemberDTO get(String username) {
    MemberVO member = Optional.ofNullable(mapper.get(username))
            .orElseThrow(NoSuchElementException::new);
    return MemberDTO.of(member);
  }

  // 아바타 저장
  private void saveAvatar(MultipartFile avatar, String loginId) {
    if (avatar != null && !avatar.isEmpty()) {
      File dest = new File("c:/upload/avatar", loginId + ".png");
      try {
        avatar.transferTo(dest);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // 회원 가입
  @Transactional
  @Override
  public MemberDTO join(MemberJoinDTO dto) {
    MemberVO member = dto.toVO();

    // 비밀번호 암호화
    member.setPassword(passwordEncoder.encode(member.getPassword()));

    // 기본 권한 설정
//    member.setAuth("ROLE_MEMBER");

    // 회원 저장
    mapper.insert(member);

    // 아바타 저장
    saveAvatar(dto.getAvatar(), member.getLoginId());

    // 저장된 회원 정보 반환
    return get(member.getLoginId());
  }

  @Override
  public Optional<MemberDTO> login(String username, String password) {
    MemberVO member = mapper.get(username);

    if (member != null && passwordEncoder.matches(password, member.getPassword())) {
      return Optional.of(MemberDTO.of(member));
    }

    return Optional.empty();  // 비밀번호 불일치 또는 사용자 없음
  }

  public MemberVO findByUsername(String loginId) {
    return mapper.findByUsername(loginId);
  }

  @Override
  public boolean resetPassword(String loginId, String password) {
    MemberVO member = mapper.get(loginId);
    if (member == null) return false;

    String encrypted = passwordEncoder.encode(password);
    mapper.resetPassword(loginId, encrypted);
    return true;
  }

  @Override
  public MemberVO findByEmail(String email) {
    return mapper.findByEmail(email);
  }


}
