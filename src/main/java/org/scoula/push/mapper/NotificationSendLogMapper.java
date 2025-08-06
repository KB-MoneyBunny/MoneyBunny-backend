package org.scoula.push.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.push.domain.NotificationSendLogVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 발송 로그 관련 데이터베이스 매퍼 (간소화 버전)
 */
public interface NotificationSendLogMapper {

    /**
     * 발송 로그 생성 (PENDING 상태로 시작)
     */
    void insertSendLog(NotificationSendLogVO sendLog);

    /**
     * 발송 상태 업데이트 (SUCCESS/FAILED)
     */
    void updateSendLogStatus(@Param("id") Long id, 
                            @Param("sendStatus") NotificationSendLogVO.SendStatus sendStatus,
                            @Param("errorMessage") String errorMessage,
                            @Param("sentAt") LocalDateTime sentAt);

    /**
     * 재시도 횟수 증가
     */
    void incrementAttemptCount(@Param("id") Long id);

    /**
     * 재전송 대상 로그 조회 (PENDING 상태이고 생성된 지 일정 시간 경과)
     */
    List<NotificationSendLogVO> findPendingLogsByTime(@Param("minutes") int minutes);

    /**
     * 특정 에러로 여러 번 실패한 토큰 조회
     * @param errorMessage 대상 에러 메시지 (예: "Requested entity was not found.")
     * @param hours 최근 몇 시간 내 로그만 조회
     * @param minFailureCount 최소 실패 횟수
     * @return 조건에 맞는 FCM 토큰 목록
     */
    List<String> findTokensByFailureCount(@Param("errorMessage") String errorMessage,
                                         @Param("hours") int hours,
                                         @Param("minFailureCount") int minFailureCount);

    /**
     * 특정 FCM 토큰의 모든 로그 삭제
     * @param token 삭제할 FCM 토큰
     */
    void deleteByToken(@Param("token") String token);
}