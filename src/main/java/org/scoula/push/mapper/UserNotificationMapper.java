package org.scoula.push.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.push.domain.UserNotificationVO;

import java.util.List;

/**
 * 사용자 알림 관련 데이터베이스 매퍼 (핵심 기능만)
 */
public interface UserNotificationMapper {

    /**
     * 새로운 알림 생성
     */
    void insertNotification(UserNotificationVO notification);

    /**
     * 특정 사용자의 미읽은 알림 조회
     */
    List<UserNotificationVO> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 모든 알림 조회
     */
    List<UserNotificationVO> findByUserId(@Param("userId") Long userId);

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
    List<UserNotificationVO> findByUserIdAndType(@Param("userId") Long userId, 
                                               @Param("type") String type);

    /**
     * 알림 ID로 단건 조회
     */
    UserNotificationVO findById(@Param("id") Long id);
}