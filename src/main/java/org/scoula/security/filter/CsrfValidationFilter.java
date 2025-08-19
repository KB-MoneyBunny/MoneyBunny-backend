package org.scoula.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * CSRF 토큰 검증 필터
 * Double Submit Cookie 패턴을 사용하여 CSRF 공격을 방지
 */
@Slf4j
@Component
public class CsrfValidationFilter extends OncePerRequestFilter {
    
    // CSRF 검증을 제외할 경로들
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/api/auth/login",           // 로그인 (토큰 발급 전)
        "/api/auth/send-join-code",  // 회원가입 이메일 인증
        "/api/auth/send-find-id-code", // 아이디 찾기
        "/api/auth/send-find-password-code", // 비밀번호 찾기
        "/api/auth/verify",          // 이메일 인증
        "/api/auth/find-id",         // 아이디 찾기 결과
        "/api/auth/reset-password",  // 비밀번호 재설정
        "/api/member/register"       // 회원가입
    );
    
    // CSRF 검증이 필요한 HTTP 메서드
    private static final List<String> VALIDATED_METHODS = Arrays.asList(
        "POST", "PUT", "DELETE", "PATCH"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        // OPTIONS 요청 (CORS 프리플라이트) 허용
        if ("OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 검증 대상 메서드가 아니면 통과
        if (!VALIDATED_METHODS.contains(method)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 제외 경로면 통과
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // CSRF 토큰 검증
        String headerToken = request.getHeader("X-CSRF-Token");
        String cookieToken = getCsrfTokenFromCookie(request);
        
        if (headerToken == null || cookieToken == null) {
            log.warn("CSRF 토큰이 누락됨 - Path: {}, Header: {}, Cookie: {}", 
                    path, headerToken != null, cookieToken != null);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token missing");
            return;
        }
        
        if (!headerToken.equals(cookieToken)) {
            log.warn("CSRF 토큰 불일치 - Path: {}", path);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token mismatch");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 요청의 Cookie에서 CSRF 토큰 추출
     */
    private String getCsrfTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        for (Cookie cookie : request.getCookies()) {
            if ("csrfToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}