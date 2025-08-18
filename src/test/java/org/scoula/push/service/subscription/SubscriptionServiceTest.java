package org.scoula.push.service.subscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.dto.request.NotificationToggleRequest;
import org.scoula.push.dto.request.SubscriptionRequest;
import org.scoula.push.dto.response.SubscriptionStatusResponse;
import org.scoula.push.mapper.SubscriptionMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService 단위 테스트")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Long userId;
    private String fcmToken;
    private SubscriptionVO subscriptionVO;
    private SubscriptionRequest subscriptionRequest;

    @BeforeEach
    void setUp() {
        userId = 1L;
        fcmToken = "test-fcm-token-123";

        subscriptionVO = new SubscriptionVO();
        subscriptionVO.setId(1L);
        subscriptionVO.setUserId(userId);
        subscriptionVO.setFcmToken(fcmToken);
        subscriptionVO.setIsActiveBookmark(true);
        subscriptionVO.setIsActiveTop3(true);
        subscriptionVO.setIsActiveNewPolicy(true);
        subscriptionVO.setIsActiveFeedback(true);
        subscriptionVO.setCreatedAt(LocalDateTime.now());

        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setToken(fcmToken);
        subscriptionRequest.setActiveBookmark(true);
        subscriptionRequest.setActiveTop3(true);
        subscriptionRequest.setActiveNewPolicy(true);
        subscriptionRequest.setActiveFeedback(true);
    }

    // ====================================
    // 구독 등록 테스트
    // ====================================

    @Test
    @DisplayName("구독 등록 - 신규 구독")
    void subscribe_NewSubscription() {
        // Given
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(null);

        // When
        subscriptionService.subscribe(userId, subscriptionRequest);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).insert(any(SubscriptionVO.class));
        verify(subscriptionMapper, never()).updateNotificationSettings(any());

        ArgumentCaptor<SubscriptionVO> captor = ArgumentCaptor.forClass(SubscriptionVO.class);
        verify(subscriptionMapper).insert(captor.capture());
        
        SubscriptionVO captured = captor.getValue();
        assertEquals(userId, captured.getUserId());
        assertEquals(fcmToken, captured.getFcmToken());
        assertTrue(captured.getIsActiveBookmark());
        assertTrue(captured.getIsActiveTop3());
    }

    @Test
    @DisplayName("구독 등록 - 기존 구독 업데이트")
    void subscribe_UpdateExisting() {
        // Given
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        subscriptionService.subscribe(userId, subscriptionRequest);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        verify(subscriptionMapper, never()).insert(any());
        
        assertEquals(userId, subscriptionVO.getUserId());
        assertTrue(subscriptionVO.getIsActiveBookmark());
    }

    @Test
    @DisplayName("구독 등록 - 다른 사용자의 토큰 재사용")
    void subscribe_ReuseTokenFromDifferentUser() {
        // Given
        Long newUserId = 2L;
        subscriptionVO.setUserId(999L); // 다른 사용자
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        subscriptionService.subscribe(newUserId, subscriptionRequest);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        assertEquals(newUserId, subscriptionVO.getUserId());
    }

    // ====================================
    // 구독 상태 조회 테스트
    // ====================================

    @Test
    @DisplayName("구독 상태 조회 - 구독 존재")
    void getSubscriptionStatus_Exists() {
        // Given
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        SubscriptionStatusResponse response = subscriptionService.getSubscriptionStatus(userId, fcmToken);

        // Then
        assertNotNull(response);
        assertTrue(response.isActiveBookmark());
        assertTrue(response.isActiveTop3());
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper, never()).updateNotificationSettings(any());
    }

    @Test
    @DisplayName("구독 상태 조회 - 구독 없음")
    void getSubscriptionStatus_NotExists() {
        // Given
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(null);

        // When
        SubscriptionStatusResponse response = subscriptionService.getSubscriptionStatus(userId, fcmToken);

        // Then
        assertNotNull(response);
        assertFalse(response.isActiveBookmark());
        assertFalse(response.isActiveTop3());
        verify(subscriptionMapper).findByToken(fcmToken);
    }

    @Test
    @DisplayName("구독 상태 조회 - 다른 사용자의 구독")
    void getSubscriptionStatus_DifferentUser() {
        // Given
        Long differentUserId = 999L;
        subscriptionVO.setUserId(differentUserId);
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        SubscriptionStatusResponse response = subscriptionService.getSubscriptionStatus(userId, fcmToken);

        // Then
        assertNotNull(response);
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        assertEquals(userId, subscriptionVO.getUserId());
        assertFalse(subscriptionVO.getIsActiveBookmark());
        assertFalse(subscriptionVO.getIsActiveTop3());
    }

    // ====================================
    // 구독 여부 확인 테스트
    // ====================================

    @Test
    @DisplayName("구독 여부 확인 - 구독중")
    void isSubscribed_True() {
        // Given
        when(subscriptionMapper.isUserSubscribed(userId)).thenReturn(true);

        // When
        boolean result = subscriptionService.isSubscribed(userId);

        // Then
        assertTrue(result);
        verify(subscriptionMapper).isUserSubscribed(userId);
    }

    @Test
    @DisplayName("구독 여부 확인 - 미구독")
    void isSubscribed_False() {
        // Given
        when(subscriptionMapper.isUserSubscribed(userId)).thenReturn(false);

        // When
        boolean result = subscriptionService.isSubscribed(userId);

        // Then
        assertFalse(result);
        verify(subscriptionMapper).isUserSubscribed(userId);
    }

    // ====================================
    // 개별 알림 토글 테스트
    // ====================================

    @Test
    @DisplayName("북마크 알림 토글 - 활성화")
    void toggleBookmarkNotification_Enable() {
        // Given
        NotificationToggleRequest request = new NotificationToggleRequest();
        request.setToken(fcmToken);
        request.setEnabled(true);
        
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        subscriptionService.toggleBookmarkNotification(userId, request);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        assertTrue(subscriptionVO.getIsActiveBookmark());
    }

    @Test
    @DisplayName("북마크 알림 토글 - 비활성화")
    void toggleBookmarkNotification_Disable() {
        // Given
        NotificationToggleRequest request = new NotificationToggleRequest();
        request.setToken(fcmToken);
        request.setEnabled(false);
        
        subscriptionVO.setIsActiveBookmark(true);
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        subscriptionService.toggleBookmarkNotification(userId, request);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        assertFalse(subscriptionVO.getIsActiveBookmark());
    }

    @Test
    @DisplayName("TOP3 알림 토글 - 활성화")
    void toggleTop3Notification_Enable() {
        // Given
        NotificationToggleRequest request = new NotificationToggleRequest();
        request.setToken(fcmToken);
        request.setEnabled(true);
        
        subscriptionVO.setIsActiveTop3(false);
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        subscriptionService.toggleTop3Notification(userId, request);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        assertTrue(subscriptionVO.getIsActiveTop3());
    }

    @Test
    @DisplayName("신규 정책 알림 토글 - 활성화")
    void toggleNewPolicyNotification_Enable() {
        // Given
        NotificationToggleRequest request = new NotificationToggleRequest();
        request.setToken(fcmToken);
        request.setEnabled(true);
        
        subscriptionVO.setIsActiveNewPolicy(false);
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        subscriptionService.toggleNewPolicyNotification(userId, request);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        assertTrue(subscriptionVO.getIsActiveNewPolicy());
    }

    @Test
    @DisplayName("피드백 알림 토글 - 비활성화")
    void toggleFeedbackNotification_Disable() {
        // Given
        NotificationToggleRequest request = new NotificationToggleRequest();
        request.setToken(fcmToken);
        request.setEnabled(false);
        
        subscriptionVO.setIsActiveFeedback(true);
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);

        // When
        subscriptionService.toggleFeedbackNotification(userId, request);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).updateNotificationSettings(subscriptionVO);
        assertFalse(subscriptionVO.getIsActiveFeedback());
    }

    // ====================================
    // 구독 취소 테스트
    // ====================================

    @Test
    @DisplayName("구독 취소 - 성공")
    void unsubscribe_Success() {
        // Given
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(subscriptionVO);
        doNothing().when(subscriptionMapper).deleteByToken(fcmToken);

        // When
        subscriptionService.unsubscribe(fcmToken);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper).deleteByToken(fcmToken);
    }

    @Test
    @DisplayName("구독 취소 - 존재하지 않는 토큰")
    void unsubscribe_TokenNotExists() {
        // Given
        when(subscriptionMapper.findByToken(fcmToken)).thenReturn(null);

        // When
        subscriptionService.unsubscribe(fcmToken);

        // Then
        verify(subscriptionMapper).findByToken(fcmToken);
        verify(subscriptionMapper, never()).deleteByToken(fcmToken);
    }

    // ====================================
    // 통합 테스트
    // ====================================

    @Test
    @DisplayName("통합 테스트 - 구독 생성 후 상태 조회")
    void integrationTest_SubscribeAndGetStatus() {
        // Given - 신규 구독
        when(subscriptionMapper.findByToken(fcmToken))
                .thenReturn(null)  // 첫 호출: 구독 없음
                .thenReturn(subscriptionVO);  // 두 번째 호출: 구독 있음

        // When - 구독 등록
        subscriptionService.subscribe(userId, subscriptionRequest);
        
        // When - 상태 조회
        SubscriptionStatusResponse status = subscriptionService.getSubscriptionStatus(userId, fcmToken);

        // Then
        assertNotNull(status);
        assertTrue(status.isActiveBookmark());
        verify(subscriptionMapper).insert(any(SubscriptionVO.class));
        verify(subscriptionMapper, times(2)).findByToken(fcmToken);
    }
}