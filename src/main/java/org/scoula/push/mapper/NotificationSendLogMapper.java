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
}