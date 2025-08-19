package org.scoula.security.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF 토큰 생성 유틸리티
 * Double Submit Cookie 패턴을 위한 랜덤 토큰 생성
 */
public class CsrfTokenUtil {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    
    /**
     * 32바이트 랜덤 CSRF 토큰 생성
     * @return Base64 인코딩된 CSRF 토큰
     */
    public static String generateToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}