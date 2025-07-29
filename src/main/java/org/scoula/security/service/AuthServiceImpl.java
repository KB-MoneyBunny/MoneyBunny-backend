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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDetailsMapper userDetailsMapper; // MemberVO 조회용 mapper
    private final AuthenticationManager authenticationManager;
    private final JwtProcessor jwtProcessor;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    // 로그인
    @Override
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
    @Override
    public void logout(String username) {
        // Redis에서 Refresh Token 삭제
        redisUtil.deleteRefreshToken("refresh_" + username);
    }


    @Override
    public boolean resetPassword(String loginId, String password) {
        MemberVO member = userDetailsMapper.get(loginId);
        if (member == null) return false;

        String encrypted = passwordEncoder.encode(password);
        userDetailsMapper.resetPassword(loginId, encrypted);
        return true;
    }

    @Override
    public MemberVO findByEmail(String email) {
        return userDetailsMapper.findByEmail(email);
    }


    // 아이디 중복 체크
    @Override
    public MemberVO findByUsername(String loginId) {
        return userDetailsMapper.findByUsername(loginId);
    }

}
