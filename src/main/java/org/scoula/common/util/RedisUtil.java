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



    // ======= (1) 범용 캐시 메서드 =======
    // 토큰·임의값 등 TTL 지정 저장 (TimeUnit 단위 지원)
    public void set(String key, String value, long timeout, TimeUnit unit) {
        if (timeout > 0) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    // TTL 미지정(영구저장)
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    // TTL(초) 단위로 반환 (없으면 -2)
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire == null ? -2 : expire;
    }




    // ======= (2) 이메일 인증 코드 전용 =======
    // 인증코드 저장 (3분)
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
