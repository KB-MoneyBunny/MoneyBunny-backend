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
  @PostMapping("/join")
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

  // 로그인
  @PostMapping("/login")
  public ResponseEntity<MemberDTO> login(@RequestBody LoginDTO loginDTO) {
    Optional<MemberDTO> memberOpt = service.login(loginDTO.getUsername(), loginDTO.getPassword());

    if (memberOpt.isPresent()) {
      return ResponseEntity.ok(memberOpt.get());
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  // 인증코드 전송(비밀번호 찾기)
  @PostMapping("/send-find-password-code")
  public ResponseEntity<String> sendVerificationCode(@RequestBody EmailPasswordResetDTO dto) {
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

    return ResponseEntity.ok("Sent code to email");
  }

  // 이메일 인증(아이디 찾기, 비밀번호 찾기 공통)
  @PostMapping("/verify")
  public ResponseEntity<?> verifyCode(@RequestBody EmailVerifyDTO request) {
    try {
      boolean isValid = redisUtil.verifyCode(request.getEmail(), request.getCode());

      if (isValid) {
        redisUtil.markVerified(request.getEmail());
        redisUtil.deleteCode(request.getEmail());
        return ResponseEntity.ok("verified");
      } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failure verification");
      }

    } catch (Exception e) {
      log.error("이메일 인증 중 예외 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("verification error");
    }
  }

  // 비밀번호 재설정
  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO dto) {
    boolean success = service.resetPassword(dto.getLoginId(), dto.getPassword());
    if (success) {
      return ResponseEntity.ok("password reset");
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user not found");
    }

  }

  // 아이디 반환(아이디 찾기)
  @PostMapping("/find-id")
  public ResponseEntity<?> findLoginId(@RequestBody EmailIDFindDTO dto) {
    // /send-find-id-code(이메일 인증 코드 전송 for id 찾기) -> /verify (인증받기) -> 아이디찾기(여기서 아이디 반환)
    String email = dto.getEmail();

    // 이메일 인증 여부 확인
    if (!redisUtil.isVerified(email)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body("not verified by this email for finding id");
    }

    // 이메일로 회원 조회
    MemberVO member = service.findByEmail(email);
    if (member == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body("not registered account by this email");
    }

    // loginId 반환
    return ResponseEntity.ok(member.getLoginId());
  }

  // 아이디 찾기
  @PostMapping("/send-find-id-code")
  public ResponseEntity<String> sendFindIdVerificationCode(@RequestBody EmailIDFindDTO dto) {
    String email = dto.getEmail();

    // 이메일로 등록된 회원 있는지 확인
    MemberVO member = service.findByEmail(email);
    if (member == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body("not registered account by this email");
    }

    // 인증 코드 생성
    String code = String.valueOf((int)(Math.random() * 900000) + 100000);

    System.out.println("redisUtil = " + redisUtil);

    // Redis에 저장 (3분 유효)
    redisUtil.saveCode(email, code);

    // 이메일 전송
    mailService.sendEmail(dto.getEmail(), "[머니버니] 본인 인증을 위한 인증 코드 안내 메일입니다.", "인증 코드: " + code);


    return ResponseEntity.ok("Sent code to email");
  }

  // 회원 가입 시 이메일 인증(이메일 db에 있으면 로그인으로 이동, db에 있으면 정상 가입)
  // EmailIDFindDTO 재활용(email만 입력한다,,)
  @PostMapping("/send-join-code")
  public ResponseEntity<String> sendJoinCode(@RequestBody EmailIDFindDTO dto) {
    String email = dto.getEmail();

    // 이미 가입된 이메일인지 확인
    if (service.findByEmail(email) != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body("Already Registered Email. Go to Login");
    }

    // 코드 생성 및 전송
    String code = String.valueOf((int)(Math.random() * 900000) + 100000);
    redisUtil.saveCode(email, code); // 3분 TTL
    mailService.sendEmail(email, "[머니버니] 본인 인증을 위한 인증 코드 안내 메일입니다.", "인증 코드: " + code);

    return ResponseEntity.ok("Sent code to email");
  }

}