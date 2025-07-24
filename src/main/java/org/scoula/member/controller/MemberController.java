package org.scoula.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.common.util.UploadFiles;
import org.scoula.member.dto.*;
import org.scoula.member.service.MailService;
import org.scoula.member.service.MemberService;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.dto.LoginDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
  final MemberService service;

  private final RedisUtil redisUtil;
  private final MailService mailService;
  // ID 중복 체크 API
  @GetMapping("/checkusername/{username}")
  public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
    return ResponseEntity.ok().body(service.checkDuplicate(username));
  }

  // 회원가입 API
  @PostMapping("/")
  public ResponseEntity<MemberDTO> join(@RequestBody MemberJoinDTO member) {
    return ResponseEntity.ok(service.join(member));
  }


  // 아바타 이미지 다운로드 API
  @GetMapping("/{username}/avatar")
  public void getAvatar(@PathVariable String username, HttpServletResponse response) {
    String avatarPath = "c:/upload/avatar/" + username + ".png";
    File file = new File(avatarPath);

    if(!file.exists()) {
      // 아바타가 없는 경우 기본 이미지 사용
      file = new File("C:/upload/avatar/unknown.png");
    }

    UploadFiles.downloadImage(response, file);
  }

  @PostMapping("/login")
  public ResponseEntity<MemberDTO> login(@RequestBody LoginDTO loginDTO) {
    Optional<MemberDTO> memberOpt = service.login(loginDTO.getUsername(), loginDTO.getPassword());

    if (memberOpt.isPresent()) {
      return ResponseEntity.ok(memberOpt.get());
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/send-code")
  public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequestDTO dto) {
    // 아이디로 사용자 조회
    MemberVO member = service.findByUsername(dto.getLoginId());

    // 아이디 - 이메일 일치하지 않으면
    if (member == null || !member.getEmail().equals(dto.getEmail())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("No matching ID and email");
    }

    // 인증 코드 생성
    String code = String.valueOf((int)(Math.random() * 900000) + 100000);

    // Redis에 저장 (3분 유효)
    redisUtil.saveCode(dto.getEmail(), code);

    // 이메일 발송
    mailService.sendEmail(dto.getEmail(), "[머니버니] 본인 인증을 위한 인증 코드 안내 메일입니다.", "인증 코드: " + code);

    return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
  }


  @PostMapping("/verify")
  public ResponseEntity<?> verifyCode(@RequestBody EmailVerifyDTO request) {
    boolean isValid = redisUtil.verifyCode(request.getEmail(), request.getCode());
    if (isValid) {
      // 검증 성공 후 Redis에서 코드 삭제 -> 바로 삭제해버리므로 swagger에서 테스트 후 터미널에서 하면 코드 삭제돼서 안 됨
      redisUtil.deleteCode(request.getEmail());
      return ResponseEntity.ok("verified");
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("failure verification");
    }
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO dto) {
    boolean success = service.resetPassword(dto.getLoginId(), dto.getPassword());
    if (success) {
      return ResponseEntity.ok("password reset");
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user not found");
    }
  }



}