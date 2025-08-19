package org.scoula.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.security.dto.AuthResultDTO;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.security.dto.UserInfoDTO;
import org.scoula.security.util.JsonResponse;
import org.scoula.security.util.JwtProcessor;
import org.scoula.security.util.CsrfTokenUtil;
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

        // Redis에 Refresh Token 저장 (14일로 수정)
        redisUtil.saveRefreshToken("refresh_" + username, refreshToken, Duration.ofDays(14));
        
        // CSRF 토큰 생성
        String csrfToken = CsrfTokenUtil.generateToken();
        
        // Refresh Token Cookie 설정
        String refreshCookieHeader = String.format(
            "refreshToken=%s; Path=/; Max-Age=%d; HttpOnly",
            refreshToken,
            14 * 24 * 60 * 60
        );
        
        // CSRF 토큰 Cookie 설정
        String csrfCookieHeader = String.format(
            "csrfToken=%s; Path=/; Max-Age=%d; HttpOnly",
            csrfToken,
            14 * 24 * 60 * 60
        );
        
        response.addHeader("Set-Cookie", refreshCookieHeader);
        response.addHeader("Set-Cookie", csrfCookieHeader);
        
        // 응답 DTO 구성 (refreshToken 제외, csrfToken 포함)
        AuthResultDTO result = AuthResultDTO.builder()
                .accessToken(accessToken)
                .username(username)
                .csrfToken(csrfToken)  // CSRF 토큰 추가
                .build();

        // JSON 응답
        JsonResponse.send(response, result);
    }
}
