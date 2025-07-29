package org.scoula.security.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.util.RedisUtil;
import org.scoula.member.service.MemberService;
import org.scoula.security.dto.LoginDTO;
import org.scoula.security.service.AuthService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtProcessor jwtProcessor;
    private final RedisUtil redisUtil;
    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        Map<String, String> tokens = authService.login(dto);
        return ResponseEntity.ok(tokens);
    }

    // refresh token 발급
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

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearer) {
        String accessToken = bearer.replace("Bearer ", "");
        String username = jwtProcessor.getUsername(accessToken);

        authService.logout(username);
        return ResponseEntity.ok("Logout success");
    }

}

