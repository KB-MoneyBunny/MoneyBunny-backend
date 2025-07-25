package org.scoula.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 알림 통계 조회 API 응답용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatsResponse {
    private String type;                    // 알림 타입
    private String typeName;                // 알림 타입 표시명
    private int totalCount;                 // 전체 알림 수
    private int readCount;                  // 읽은 알림 수
    private int unreadCount;                // 읽지 않은 알림 수
    private double readRate;                // 읽음율 (%)

    /**
     * 읽음율 계산 후 설정
     */
    public void calculateReadRate() {
        if (totalCount > 0) {
            this.readRate = Math.round((double) readCount / totalCount * 100 * 100.0) / 100.0;
        } else {
            this.readRate = 0.0;
        }
    }
}