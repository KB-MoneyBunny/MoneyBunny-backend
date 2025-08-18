package org.scoula.codef.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.common.util.RedisUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodefTokenService 단위 테스트")
class CodefTokenServiceTest {

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private CodefTokenService codefTokenService;

    private final String clientId = "test-client-id";
    private final String clientSecret = "test-client-secret";
    private final String mockToken = "mock-access-token-123456";

    @BeforeEach
    void setUp() {
        // ReflectionTestUtils를 사용하여 private 필드 값 설정
        ReflectionTestUtils.setField(codefTokenService, "clientId", clientId);
        ReflectionTestUtils.setField(codefTokenService, "clientSecret", clientSecret);
    }

    // ====================================
    // 토큰 조회 테스트 - Redis 캐시된 토큰
    // ====================================

    @Test
    @DisplayName("토큰 조회 - Redis에서 캐시된 토큰 반환")
    void getAccessToken_FromRedisCache() {
        // Given
        when(redisUtil.get("codef:accessToken")).thenReturn(mockToken);

        // When
        String result = codefTokenService.getAccessToken();

        // Then
        assertEquals(mockToken, result);
        verify(redisUtil).get("codef:accessToken");
        // 캐시된 토큰이 있으므로 Redis set은 호출되지 않아야 함
        verify(redisUtil, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("토큰 조회 - Redis에 캐시된 빈 토큰")
    void getAccessToken_EmptyTokenInCache() {
        // Given
        when(redisUtil.get("codef:accessToken")).thenReturn("");

        // When & Then
        // 빈 문자열은 null이 아니므로 빈 문자열이 반환됨
        String result = codefTokenService.getAccessToken();
        assertEquals("", result);
        verify(redisUtil).get("codef:accessToken");
    }

    @Test
    @DisplayName("토큰 조회 - Redis 캐시 없음 (실제 API 호출)")
    void getAccessToken_NoCacheApiCall() {
        // Given
        when(redisUtil.get("codef:accessToken")).thenReturn(null);

        // When & Then
        // Redis에 캐시가 없으면 실제 CODEF API를 호출하므로 RuntimeException 발생 예상
        // (테스트 환경에서는 실제 API 호출이 실패함)
        assertThrows(RuntimeException.class, () -> {
            codefTokenService.getAccessToken();
        });

        verify(redisUtil).get("codef:accessToken");
    }

    // ====================================
    // 경계 조건 테스트
    // ====================================

    @Test
    @DisplayName("토큰 조회 - null 토큰 캐시")
    void getAccessToken_NullTokenCache() {
        // Given
        when(redisUtil.get("codef:accessToken")).thenReturn(null);

        // When & Then
        // null이면 새로 토큰을 발급받으려 시도하므로 RuntimeException 발생 예상
        assertThrows(RuntimeException.class, () -> {
            codefTokenService.getAccessToken();
        });

        verify(redisUtil).get("codef:accessToken");
    }

    @Test
    @DisplayName("토큰 조회 - Redis 예외 처리")
    void getAccessToken_RedisException() {
        // Given
        when(redisUtil.get("codef:accessToken")).thenThrow(new RuntimeException("Redis connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            codefTokenService.getAccessToken();
        });

        verify(redisUtil).get("codef:accessToken");
    }

    // ====================================
    // 상수 및 키 검증
    // ====================================

    @Test
    @DisplayName("Redis 키 상수 검증")
    void verifyRedisKey() {
        // Given
        when(redisUtil.get("codef:accessToken")).thenReturn(mockToken);

        // When
        codefTokenService.getAccessToken();

        // Then
        // 정확한 Redis 키가 사용되는지 확인
        verify(redisUtil).get("codef:accessToken");
    }

    @Test
    @DisplayName("클라이언트 ID/Secret 설정 검증")
    void verifyClientCredentials() {
        // Given/When
        String actualClientId = (String) ReflectionTestUtils.getField(codefTokenService, "clientId");
        String actualClientSecret = (String) ReflectionTestUtils.getField(codefTokenService, "clientSecret");

        // Then
        assertEquals(clientId, actualClientId);
        assertEquals(clientSecret, actualClientSecret);
    }
}