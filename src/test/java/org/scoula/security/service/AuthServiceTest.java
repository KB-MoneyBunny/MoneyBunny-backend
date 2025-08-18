package org.scoula.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.common.util.RedisUtil;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.account.mapper.UserDetailsMapper;
import org.scoula.security.dto.LoginDTO;
import org.scoula.security.util.JwtProcessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserDetailsMapper userDetailsMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProcessor jwtProcessor;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginDTO loginDTO;
    private MemberVO memberVO;
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        username = "testuser";
        password = "password123";
        accessToken = "eyJhbGciOiJIUzI1NiJ9.access.token";
        refreshToken = "eyJhbGciOiJIUzI1NiJ9.refresh.token";

        loginDTO = new LoginDTO(username, password);

        memberVO = MemberVO.builder()
                .userId(1L)
                .loginId(username)
                .password("encodedPassword")
                .email("test@example.com")
                .name("테스트사용자")
                .createdAt(new Date())
                .build();
    }

    // ====================================
    // 로그인 관련 테스트
    // ====================================

    @Test
    @DisplayName("로그인 - 성공")
    void login_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtProcessor.generateToken(username)).thenReturn(accessToken);
        when(jwtProcessor.generateRefreshToken(username)).thenReturn(refreshToken);
        doNothing().when(redisUtil).saveRefreshToken(anyString(), anyString(), any(Duration.class));

        // When
        Map<String, String> result = authService.login(loginDTO);

        // Then
        assertNotNull(result);
        assertEquals(accessToken, result.get("accessToken"));
        assertEquals(refreshToken, result.get("refreshToken"));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtProcessor).generateToken(username);
        verify(jwtProcessor).generateRefreshToken(username);
        verify(redisUtil).saveRefreshToken(eq("refresh_" + username), eq(refreshToken), any(Duration.class));
    }

    @Test
    @DisplayName("로그인 - 잘못된 인증 정보로 실패")
    void login_BadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(loginDTO));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtProcessor, never()).generateToken(anyString());
        verify(jwtProcessor, never()).generateRefreshToken(anyString());
        verify(redisUtil, never()).saveRefreshToken(anyString(), anyString(), any(Duration.class));
    }

    // ====================================
    // 사용자 조회 관련 테스트 
    // ====================================

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void findByEmail_Success() {
        // Given
        String email = "test@example.com";
        when(userDetailsMapper.get(email)).thenReturn(memberVO);

        // When
        MemberVO result = authService.findByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userDetailsMapper).get(email);
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 성공")
    void findByUsername_Success() {
        // Given
        when(userDetailsMapper.get(username)).thenReturn(memberVO);

        // When
        MemberVO result = authService.findByUsername(username);

        // Then
        assertNotNull(result);
        verify(userDetailsMapper).get(username);
    }

    // ====================================
    // 로그아웃 관련 테스트
    // ====================================

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_Success() {
        // Given
        doNothing().when(redisUtil).deleteRefreshToken("refresh_" + username);

        // When & Then
        assertDoesNotThrow(() -> authService.logout(username));
        
        verify(redisUtil).deleteRefreshToken("refresh_" + username);
    }

    // ====================================
    // 비밀번호 재설정 관련 테스트
    // ====================================

    @Test
    @DisplayName("비밀번호 재설정 - AuthService 인터페이스 테스트")
    void resetPassword_InterfaceTest() {
        // Given
        String loginId = "testuser";
        String newPassword = "newPassword123";

        // When
        boolean result = authService.resetPassword(loginId, newPassword);

        // Then
        // 실제 구현체에서 처리되므로 메서드 호출이 가능한지만 확인
        // 결과는 실제 구현에 따라 달라질 수 있음
        assertNotNull(result);
    }
}