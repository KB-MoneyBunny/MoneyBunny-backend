package org.scoula.security.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.util.RedisUtil;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtProcessor jwtProcessor;
    private final RedisUtil redisUtil;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String bearer) {
        String refreshToken = bearer.replace("Bearer ", "");

        if (!jwtProcessor.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token 만료 또는 위조");
        }

        String loginId = jwtProcessor.getUsername(refreshToken);
        String stored = redisUtil.getRefreshToken("refresh_" + loginId);

        if (!refreshToken.equals(stored)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("서버 저장 토큰과 불일치");
        }

        String newAccessToken = jwtProcessor.generateToken(loginId);
        return ResponseEntity.ok().body(Collections.singletonMap("accessToken", newAccessToken));
    }
}

