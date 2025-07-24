package org.scoula.push.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * subscription 테이블과 매핑되는 엔티티
 */
@Data
public class Subscription {
    private Long id;
    private Long userId;
    private String endpoint; // FCM 토큰
    private boolean isActive;
    private LocalDateTime createdAt;
}