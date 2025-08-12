package org.scoula.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;
    
    // 일일 조회 기록 TTL (2일 - 배치 처리 실패 대비)
    private static final long DAILY_VIEW_TTL_DAYS = 2;
    
    // 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

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
    // 📌 사용자별 일일 정책 조회 기록 관련
    // ────────────────────────────────────────

    /**
     * 사용자의 일일 정책 조회 기록 (하루 단위로 조회수 카운트)
     * 키: user:daily:{userId}:{date}:{policyId} → count
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @return 오늘 해당 정책 조회수
     */
    public Long recordDailyPolicyView(Long userId, Long policyId) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = String.format("user:daily:%d:%s:%d", userId, today, policyId);
        
        Long count = redisTemplate.opsForValue().increment(key);
        // 첫 조회시에만 TTL 설정 (2일)
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofDays(DAILY_VIEW_TTL_DAYS));
        }
        
        log.trace("일일 정책 조회 기록 - userId: {}, policyId: {}, date: {}, count: {}", 
                userId, policyId, today, count);
        return count;
    }

    /**
     * 특정 날짜의 사용자가 조회한 정책 목록 조회
     * @param userId 사용자 ID
     * @param date 날짜 (yyyyMMdd 형식)
     * @return 해당 날짜에 조회한 정책 ID와 조회수 목록
     */
    public Set<String> getUserDailyViewKeys(Long userId, String date) {
        String pattern = String.format("user:daily:%d:%s:*", userId, date);
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys : Set.of();
    }

    /**
     * 특정 날짜의 특정 사용자-정책 조회수 조회
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @param date 날짜 (yyyyMMdd 형식)
     * @return 조회수 (없으면 0)
     */
    public Long getDailyViewCount(Long userId, Long policyId, String date) {
        String key = String.format("user:daily:%d:%s:%d", userId, date, policyId);
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0L;
    }
    
    /**
     * 특정 날짜의 모든 사용자 일일 조회 키 목록 조회 (배치 처리용)
     * @param date 날짜 (yyyyMMdd 형식)
     * @return 해당 날짜의 모든 조회 키 Set
     */
    public Set<String> getAllDailyViewKeys(String date) {
        String pattern = String.format("user:daily:*:%s:*", date);
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys : Set.of();
    }
    
    /**
     * 특정 날짜의 사용자별 조회 데이터 삭제 (배치 처리 완료 후)
     * @param userId 사용자 ID
     * @param date 날짜 (yyyyMMdd 형식)
     */
    public void deleteDailyViewData(Long userId, String date) {
        try {
            Set<String> keys = getUserDailyViewKeys(userId, date);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("일일 조회 데이터 삭제 완료 - userId: {}, date: {}, 삭제 수: {}", 
                        userId, date, keys.size());
            }
        } catch (Exception e) {
            log.error("일일 조회 데이터 삭제 실패 - userId: {}, date: {}, 오류: {}", 
                    userId, date, e.getMessage());
        }
    }
    
    /**
     * 키에서 사용자 ID와 정책 ID 추출 (배치 처리용)
     * @param key Redis 키 (user:daily:{userId}:{date}:{policyId})
     * @return [userId, policyId] 배열
     */
    public Long[] extractIdsFromKey(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 5) {
            Long userId = Long.parseLong(parts[2]);
            Long policyId = Long.parseLong(parts[4]);
            return new Long[]{userId, policyId};
        }
        return null;
    }

    // ────────────────────────────────────────
    // 📌 좋아요 시스템 관련 (Redis Set 활용) - 핵심 기능만
    // ────────────────────────────────────────

    /**
     * 리뷰에 좋아요 추가
     * @param userId 사용자 ID
     * @param reviewId 리뷰 ID
     * @return true: 새로 추가됨, false: 이미 좋아요 상태
     */
    public boolean addLikeToReview(Long userId, Long reviewId) {
        String reviewLikesKey = String.format("review:likes:%d", reviewId);
        
        // Redis에 좋아요 추가 (Set에 userId 추가)
        Long added = redisTemplate.opsForSet().add(reviewLikesKey, userId.toString());
        
        if (added > 0) {
            log.info("좋아요 추가 완료 - userId: {}, reviewId: {}", userId, reviewId);
            return true;
        } else {
            log.debug("이미 좋아요한 리뷰입니다. userId: {}, reviewId: {}", userId, reviewId);
            return false;
        }
    }

    /**
     * 리뷰 좋아요 취소
     * @param userId 사용자 ID
     * @param reviewId 리뷰 ID
     * @return true: 취소됨, false: 좋아요 상태가 아니었음
     */
    public boolean removeLikeFromReview(Long userId, Long reviewId) {
        String reviewLikesKey = String.format("review:likes:%d", reviewId);
        
        // Redis에서 좋아요 제거 (Set에서 userId 제거)
        Long removed = redisTemplate.opsForSet().remove(reviewLikesKey, userId.toString());
        
        if (removed > 0) {
            log.info("좋아요 취소 완료 - userId: {}, reviewId: {}", userId, reviewId);
            return true;
        } else {
            log.debug("좋아요 상태가 아닌 리뷰입니다. userId: {}, reviewId: {}", userId, reviewId);
            return false;
        }
    }

    /**
     * 리뷰의 좋아요 수 조회 (Set 크기로 계산)
     * @param reviewId 리뷰 ID
     * @return 좋아요 수
     */
    public Long getLikeCount(Long reviewId) {
        String reviewLikesKey = String.format("review:likes:%d", reviewId);
        Long setSize = redisTemplate.opsForSet().size(reviewLikesKey);
        return setSize != null ? setSize : 0L;
    }

}
