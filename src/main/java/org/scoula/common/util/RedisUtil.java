package org.scoula.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveCode(String email, String code) {
        log.info("Redis에 인증코드 저장: {} => {}", email, code);
        try {
            redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(3));
        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
        }
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(email);
        return storedCode != null && storedCode.equals(code);


    }
    // 인증 코드 삭제
    public void deleteCode(String email) {
        redisTemplate.delete(email);
    }

    // 인증 성공 시 플래그 (유효 시간: 3분)
    public void markVerified(String email) {
        redisTemplate.opsForValue().set("verified:" + email, "true", 3, TimeUnit.MINUTES);
    }

    // 인증 여부 확인
    public boolean isVerified(String email) {
        String key = "verified:" + email;
        String result = redisTemplate.opsForValue().get(key);
        return "true".equals(result);
    }

}
