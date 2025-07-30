package org.scoula.security.service;

import lombok.RequiredArgsConstructor;
import org.scoula.common.util.RedisUtil;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.dto.LoginDTO;
import org.scoula.security.util.JwtProcessor;
import org.scoula.security.account.mapper.UserDetailsMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsMapper userDetailsMapper; // MemberVO 조회용 mapper
    private final AuthenticationManager authenticationManager;
    private final JwtProcessor jwtProcessor;
    private final RedisUtil redisUtil;

    // 로그인
    public Map<String, String> login(LoginDTO loginDTO) {
        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );

        // 토큰 생성
        String accessToken = jwtProcessor.generateToken(loginDTO.getUsername());
        String refreshToken = jwtProcessor.generateRefreshToken(loginDTO.getUsername());

        // Redis에 Refresh Token 저장 (e.g. 7일 TTL)
        redisUtil.saveRefreshToken("refresh_" + loginDTO.getUsername(), refreshToken, Duration.ofDays(7));

        // 토큰 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    // 로그아웃
    public void logout(String username) {
        // Redis에서 Refresh Token 삭제
        redisUtil.deleteRefreshToken("refresh_" + username);
    }


}
