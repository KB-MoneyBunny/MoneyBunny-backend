package org.scoula.security.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.security.service.MailService;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.dto.*;
import org.scoula.security.service.AuthServiceImpl;
import org.scoula.security.util.JwtProcessor;
import org.scoula.security.util.PasswordValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Api(tags = "인증 및 보안 API", description = "로그인, 이메일 인증, 비밀번호 재설정, 토큰 재발급 등 인증 관련 API")
public class AuthController {


    private final JwtProcessor jwtProcessor;
    private final RedisUtil redisUtil;
    private final AuthServiceImpl authService;
    private final MailService mailService;

    // 로그인 및 로그아웃

    // 로그인
    @ApiOperation(value = "로그인", notes = "사용자 로그인 후 Access Token과 Refresh Token을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        Map<String, String> tokens = authService.login(dto);
        return ResponseEntity.ok(tokens);
    }

    // 로그아웃
    @ApiOperation(value = "로그아웃", notes = "로그아웃 처리 후 서버에 저장된 Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearer) {
        String accessToken = bearer.replace("Bearer ", "");
        String username = jwtProcessor.getUsername(accessToken);

        authService.logout(username);
        return ResponseEntity.ok("Logout success");
    }

    // 로그인
//    @PostMapping("/login")
//    public ResponseEntity<MemberDTO> login(@RequestBody LoginDTO loginDTO) {
//        Optional<MemberDTO> memberOpt = service.login(loginDTO.getUsername(), loginDTO.getPassword());
//
//        if (memberOpt.isPresent()) {
//            return ResponseEntity.ok(memberOpt.get());
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//    }

    // 이메일 인증 관련

    // 회원 가입 시 이메일 인증(이메일 db에 있으면 로그인으로 이동, db에 있으면 정상 가입)
    // EmailIDFindDTO 재활용(email만 입력한다,,)
    @ApiOperation(value = "회원가입 시 이메일 인증코드 전송", notes = "회원가입 전 입력된 이메일이 기존 회원이 아니면 인증코드를 전송합니다.")
    @PostMapping("/send-join-code")
    public ResponseEntity<String> sendJoinCode(@RequestBody EmailIDFindDTO dto) {
        String email = dto.getEmail();

        // 이미 가입된 이메일인지 확인
        if (authService.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Already Registered Email. Go to Login");
        }

        // 코드 생성 및 전송
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        redisUtil.saveCode(email, code); // 3분 TTL
        mailService.sendEmail(email, "[머니버니] 본인 인증을 위한 인증 코드 안내 메일입니다.", "인증 코드: " + code);

        return ResponseEntity.ok("Sent code to email");
    }

    // 인증코드 전송(아이디 찾기)
    @ApiOperation(value = "아이디 찾기 - 인증코드 전송", notes = "입력된 이메일이 회원정보에 존재하면 인증코드를 해당 이메일로 전송합니다.")
    @PostMapping("/send-find-id-code")
    public ResponseEntity<String> sendFindIdVerificationCode(@RequestBody EmailIDFindDTO dto) {
        String email = dto.getEmail();

        // 이메일로 등록된 회원 있는지 확인
        MemberVO member = authService.findByEmail(email);
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

    // 인증코드 전송(비밀번호 찾기)
    @ApiOperation(value = "비밀번호 찾기 - 인증코드 전송", notes = "아이디와 이메일이 일치하면 해당 이메일로 비밀번호 재설정을 위한 인증코드를 전송합니다.")
    @PostMapping("/send-find-password-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailPasswordResetDTO dto) {
        // 아이디로 사용자 조회
        MemberVO member = authService.findByUsername(dto.getLoginId());

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
    @ApiOperation(value = "이메일 인증코드 검증", notes = "이메일로 전송된 인증코드가 올바른지 검증합니다. 아이디/비밀번호 찾기 및 회원가입 인증에 사용됩니다.")
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

    // 인증 후 액션

    // 아이디 반환(아이디 찾기)
    @ApiOperation(value = "아이디 찾기 - 결과 반환", notes = "이메일 인증을 완료한 사용자에게 해당 이메일로 등록된 아이디를 반환합니다.")
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
        MemberVO member = authService.findByEmail(email);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("not registered account by this email");
        }

        // loginId 반환
        return ResponseEntity.ok(member.getLoginId());
    }

    // 비밀번호 재설정
    @ApiOperation(value = "비밀번호 재설정", notes = "이메일 인증을 마친 사용자에 대해 새로운 비밀번호로 재설정합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO dto) {
        // 비밀번호 유효성 검사
        if (!PasswordValidator.isValid(dto.getPassword())) {
            return ResponseEntity.badRequest().body("비밀번호 형식이 유효하지 않습니다.");
        }

        // 정상 처리
        try {
            boolean success = authService.resetPassword(dto.getLoginId(), dto.getPassword());
            if (success) {
                return ResponseEntity.ok("password reset");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user not found");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    // Access Token, Refresh Token

    // Access Token 재발급
    @ApiOperation(value = "Access Token 재발급", notes = "유효한 Refresh Token을 통해 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String bearer) {
        // "Bearer " 접두어 제거 후 실제 토큰 추출
        String refreshToken = bearer.replace("Bearer ", "");

        // Refresh Token 유효성 검증 (서명 및 만료 확인)
        if (!jwtProcessor.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token expired or faked");
        }

        // 토큰에서 loginId 추출
        String loginId = jwtProcessor.getUsername(refreshToken);

        // Redis에 저장된 refresh 토큰과 비교
        String stored = redisUtil.getRefreshToken("refresh_" + loginId);
        if (!refreshToken.equals(stored)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("not match stored token");
        }

        // 새 Access Token 발급 후 응답
        String newAccessToken = jwtProcessor.generateToken(loginId);

        return ResponseEntity.ok().body(Collections.singletonMap("accessToken", newAccessToken));
    }


    // 토큰 유효 여부 확인
    @ApiOperation(value = "Refresh Token 유효성 검사", notes = "전송된 Refresh Token의 만료 여부를 확인합니다.")
    @PostMapping("/check-refresh-token")
    public ResponseEntity<String> checkRefreshToken(@RequestHeader("Authorization") String token) {
        // Bearer 토큰이면 "Bearer " 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (jwtProcessor.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token is expired. Please log in again.");
        }

        return ResponseEntity.ok("Refresh token is still valid.");
    }

}

