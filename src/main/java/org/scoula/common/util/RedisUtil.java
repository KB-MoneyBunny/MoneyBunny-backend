package org.scoula.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

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

    public void deleteCode(String email) {
        redisTemplate.delete(email);
    }
}
