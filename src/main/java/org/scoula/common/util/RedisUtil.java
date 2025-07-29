package org.scoula.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 인증코드 저장
    public void saveCode(String email, String code) {
        log.info("Redis에 인증코드 저장: {} => {}", email, code);
        try {
            redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(3));
            String result = redisTemplate.opsForValue().get(email); // 바로 읽음
            log.info("저장 후 조회 결과: {}", result); // null이면 저장 안 됨

        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
        }
    }

    // 인증 코드 인증
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

    // RefreshToken
    // Refresh Token 저장
    public void saveRefreshToken(String username, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set("refresh:" + username, refreshToken, ttl);
    }

    // Refresh Token 조회
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get("refresh:" + username);
    }

    // Refresh Token 삭제 (로그아웃 등)
    public void deleteRefreshToken(String username) {
        redisTemplate.delete("refresh:" + username);
    }

    // ────────────────────────────────────────
    // 📌 사용자별 정책 조회 기록 관련
    // ────────────────────────────────────────

    /**
     * 사용자-정책별 조회수 증가 (키: user:view:{userId}:{policyId}, TTL: 30일)
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @return 증가된 조회수
     */
    public Long incrementUserPolicyView(Long userId, Long policyId) {
        String key = "user:view:" + userId + ":" + policyId;
        Long count = redisTemplate.opsForValue().increment(key);
        // 30일 TTL 설정
        redisTemplate.expire(key, Duration.ofDays(30));
        log.debug("사용자 정책 조회수 증가 - userId: {}, policyId: {}, count: {}", userId, policyId, count);
        return count;
    }

    /**
     * 사용자 조회 정책 목록에 추가 (키: user:view:{userId}, Set 타입, TTL: 30일)
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     */
    public void addToUserViewList(Long userId, Long policyId) {
        String key = "user:view:" + userId;
        redisTemplate.opsForSet().add(key, policyId.toString());
        // 30일 TTL 설정
        redisTemplate.expire(key, Duration.ofDays(30));
        log.debug("사용자 조회 목록에 추가 - userId: {}, policyId: {}", userId, policyId);
    }

    /**
     * 사용자-정책별 조회수 조회
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @return 조회수 (없으면 0)
     */
    public Long getUserPolicyViewCount(Long userId, Long policyId) {
        String key = "user:view:" + userId + ":" + policyId;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0L;
    }

    /**
     * 사용자가 조회한 정책 목록 조회
     * @param userId 사용자 ID
     * @return 조회한 정책 ID Set (없으면 빈 Set)
     */
    public Set<String> getUserViewedPolicies(Long userId) {
        String key = "user:view:" + userId;
        Set<String> policies = redisTemplate.opsForSet().members(key);
        return policies != null ? policies : Set.of();
    }

    /**
     * 모든 사용자 조회 키 목록 조회 (배치 처리용, user:view:* 패턴)
     * @return 사용자 조회 키 Set
     */
    public Set<String> getAllUserViewKeys() {
        Set<String> keys = redisTemplate.keys("user:view:*");
        return keys != null ? keys : Set.of();
    }

    /**
     * 사용자별 조회 데이터 삭제 (조회 목록 + 개별 조회수)
     * @param userId 삭제할 사용자 ID
     */
    public void deleteUserViewData(Long userId) {
        try {
            // 사용자 조회 목록 삭제
            String listKey = "user:view:" + userId;
            redisTemplate.delete(listKey);
            
            // 개별 조회수 데이터 삭제
            Set<String> policyIds = getUserViewedPolicies(userId);
            for (String policyId : policyIds) {
                String countKey = "user:view:" + userId + ":" + policyId;
                redisTemplate.delete(countKey);
            }
            
            log.info("사용자 조회 데이터 삭제 완료 - userId: {}, 정책 수: {}", userId, policyIds.size());
        } catch (Exception e) {
            log.error("사용자 조회 데이터 삭제 실패 - userId: {}, 오류: {}", userId, e.getMessage());
        }
    }

}
