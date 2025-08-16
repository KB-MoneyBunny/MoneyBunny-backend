package org.scoula.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.security.dto.AuthResultDTO;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.security.dto.UserInfoDTO;
import org.scoula.security.util.JsonResponse;
import org.scoula.security.util.JwtProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;


@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    // JWT 처리용 유틸리티 클래스 의존성 주입
    private final JwtProcessor jwtProcessor;
    // Redis
    private final RedisUtil redisUtil;
    /**
     * 인증 성공 결과 생성 메서드
     * @param user
     * @return AuthResultDTO
     */
//    private AuthResultDTO makeAuthResult(CustomUser user) {
//        String username = user.getUsername();
//
//        // JWT 토큰 생성
//        String token = jwtProcessor.generateToken(username);
//
//        // 토큰 + 사용자 기본 정보를 AuthResultDTO로 구성
//        return new AuthResultDTO(token, UserInfoDTO.of(user.getMember()));
//    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 사용자 정보 가져오기
        CustomUser user = (CustomUser) authentication.getPrincipal();
        String username = user.getUsername();
//        String role = user.getAuthorities().iterator().next().getAuthority();

        // Access + Refresh Token 생성
        String accessToken = jwtProcessor.generateToken(username);
        String refreshToken = jwtProcessor.generateRefreshToken(username);

        // Redis에 Refresh Token 저장 (14d)
        redisUtil.saveRefreshToken("refresh_" + username, refreshToken, Duration.ofDays(14));

        // 응답 DTO 구성
        AuthResultDTO result = AuthResultDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(username)
                .build();

        // JSON 응답
        JsonResponse.send(response, result);
    }
}
