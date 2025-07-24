package org.scoula.push.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.push.domain.UserNotification;

import java.util.List;

/**
 * 사용자 알림 관련 데이터베이스 매퍼 (핵심 기능만)
 */
public interface UserNotificationMapper {

    /**
     * 새로운 알림 생성
     */
    void insertNotification(UserNotification notification);

    /**
     * 특정 사용자의 미읽은 알림 조회
     */
    List<UserNotification> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 모든 알림 조회
     */
    List<UserNotification> findByUserId(@Param("userId") Long userId);

    /**
     * 알림 읽음 처리 (ID만으로 단순 처리)
     */
    void markAsRead(@Param("notificationId") Long notificationId);

    /**
     * 특정 사용자의 미읽은 알림 개수
     */
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 타입 알림 조회
     */
    List<UserNotification> findByUserIdAndType(@Param("userId") Long userId, 
                                               @Param("type") String type);
}