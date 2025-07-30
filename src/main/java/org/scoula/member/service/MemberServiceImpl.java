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

  @Override
  public MemberVO findByUsername(String loginId) {
    return mapper.findByUsername(loginId);
  }
  // 회원 가입
  @Transactional
  @Override
  public MemberDTO join(MemberJoinDTO dto) {

    validateJoinInfo(dto); // 유효성 검사 추가

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

  private void validateJoinInfo(MemberJoinDTO dto) {
    String email = dto.getEmail();
    String password = dto.getPassword();

    // 이메일 유효성
    if (email == null || !email.contains("@")) {
      throw new IllegalArgumentException("유효한 이메일 주소를 입력해주세요.");
    }

    if (mapper.isEmailExists(email)) {
      throw new IllegalArgumentException("이미 가입된 이메일입니다.");
    }

    // 비밀번호 유효성
    if (password == null || password.length() < 10) {
      throw new IllegalArgumentException("비밀번호는 10자 이상이어야 합니다.");
    }

    boolean hasLetter = password.matches(".*[A-Za-z].*");
    boolean hasDigit = password.matches(".*[0-9].*");
    boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>~`\\-_=+\\\\/\\[\\]].*");

    if (!hasLetter || !hasDigit || !hasSpecial) {
      throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
    }
  }



}
