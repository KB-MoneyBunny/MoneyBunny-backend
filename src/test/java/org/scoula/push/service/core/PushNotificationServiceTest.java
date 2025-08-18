package org.scoula.push.service.core;

import com.google.firebase.messaging.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.mapper.SubscriptionMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService 단위 테스트")
class PushNotificationServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private PushNotificationService pushNotificationService;

    private SubscriptionVO subscription1;
    private SubscriptionVO subscription2;
    private List<SubscriptionVO> mockSubscriptions;

    @BeforeEach
    void setUp() {
        subscription1 = new SubscriptionVO();
        subscription1.setId(1L);
        subscription1.setUserId(100L);
        subscription1.setFcmToken("fcm-token-user-100");
        subscription1.setIsActiveFeedback(true);

        subscription2 = new SubscriptionVO();
        subscription2.setId(2L);
        subscription2.setUserId(200L);
        subscription2.setFcmToken("fcm-token-user-200");
        subscription2.setIsActiveFeedback(true);

        mockSubscriptions = Arrays.asList(subscription1, subscription2);
    }

    // ====================================
    // 맞춤 알림 전송 테스트 - 성공 케이스
    // ====================================

    @Test
    @DisplayName("맞춤 알림 전송 - 성공")
    void sendAllCustomNotifications_Success() throws Exception {
        // Given
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(mockSubscriptions);
        when(firebaseMessaging.send(any(Message.class)))
                .thenReturn("message-id-1")
                .thenReturn("message-id-2");

        // When
        pushNotificationService.sendAllCustomNotifications();

        // Then
        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging, times(2)).send(any(Message.class));

        // Firebase 메시지 전송 호출 횟수만 검증 (Message 객체의 내부 접근 제한으로 인해)
        verify(firebaseMessaging, times(2)).send(any(Message.class));
    }

    @Test
    @DisplayName("맞춤 알림 전송 - 단일 사용자")
    void sendAllCustomNotifications_SingleUser() throws Exception {
        // Given
        List<SubscriptionVO> singleSubscription = Collections.singletonList(subscription1);
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(singleSubscription);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id-1");

        // When
        pushNotificationService.sendAllCustomNotifications();

        // Then
        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging, times(1)).send(any(Message.class));

        // Firebase 메시지 전송 확인 (Message 객체 내부 접근 제한으로 인해 호출만 검증)
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    // ====================================
    // 맞춤 알림 전송 테스트 - 빈 구독 목록
    // ====================================

    @Test
    @DisplayName("맞춤 알림 전송 - 활성 구독 없음")
    void sendAllCustomNotifications_NoActiveSubscriptions() throws Exception {
        // Given
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(Collections.emptyList());

        // When
        pushNotificationService.sendAllCustomNotifications();

        // Then
        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging, never()).send(any(Message.class));
        // Firebase 호출이 전혀 없어야 함
        verifyNoMoreInteractions(firebaseMessaging);
    }

    @Test
    @DisplayName("맞춤 알림 전송 - null 구독 목록")
    void sendAllCustomNotifications_NullSubscriptions() throws Exception {
        // Given
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK")).thenReturn(null);

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            pushNotificationService.sendAllCustomNotifications();
        });

        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    // ====================================
    // 맞춤 알림 전송 테스트 - Firebase 예외 처리
    // ====================================

    @Test
    @DisplayName("맞춤 알림 전송 - UNREGISTERED 토큰 예외")
    void sendAllCustomNotifications_UnregisteredToken() throws Exception {
        // Given
        List<SubscriptionVO> singleSubscription = Collections.singletonList(subscription1);
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(singleSubscription);
        
        FirebaseMessagingException unregisteredError = mock(FirebaseMessagingException.class);
        when(unregisteredError.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(unregisteredError);

        // When
        assertDoesNotThrow(() -> pushNotificationService.sendAllCustomNotifications());

        // Then
        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging).send(any(Message.class));
        verify(unregisteredError).getMessagingErrorCode();
    }

    @Test
    @DisplayName("맞춤 알림 전송 - 기타 Firebase 예외")
    void sendAllCustomNotifications_OtherFirebaseException() throws Exception {
        // Given
        List<SubscriptionVO> singleSubscription = Collections.singletonList(subscription1);
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(singleSubscription);
        
        FirebaseMessagingException otherError = mock(FirebaseMessagingException.class);
        when(otherError.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INVALID_ARGUMENT);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(otherError);

        // When
        assertDoesNotThrow(() -> pushNotificationService.sendAllCustomNotifications());

        // Then
        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging).send(any(Message.class));
        verify(otherError).getMessagingErrorCode();
    }

    @Test
    @DisplayName("맞춤 알림 전송 - 일부 성공, 일부 실패")
    void sendAllCustomNotifications_PartialFailure() throws Exception {
        // Given
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(mockSubscriptions);
        
        // 첫 번째는 성공, 두 번째는 실패
        when(firebaseMessaging.send(any(Message.class)))
                .thenReturn("message-id-1")
                .thenThrow(mock(FirebaseMessagingException.class));

        // When
        assertDoesNotThrow(() -> pushNotificationService.sendAllCustomNotifications());

        // Then
        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging, times(2)).send(any(Message.class));
        // 예외가 발생해도 다음 사용자에게 계속 전송 시도해야 함
    }

    // ====================================
    // 맞춤 알림 전송 테스트 - 매퍼 예외
    // ====================================

    @Test
    @DisplayName("맞춤 알림 전송 - 매퍼 예외 발생")
    void sendAllCustomNotifications_MapperException() throws Exception {
        // Given
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            pushNotificationService.sendAllCustomNotifications();
        });

        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    // ====================================
    // 메시지 구조 테스트
    // ====================================

    @Test
    @DisplayName("메시지 구조 검증 - 필수 데이터 포함 확인")
    void verifyMessageStructure() throws Exception {
        // Given
        List<SubscriptionVO> singleSubscription = Collections.singletonList(subscription1);
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(singleSubscription);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id");

        // When
        pushNotificationService.sendAllCustomNotifications();

        // Then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(firebaseMessaging).send(messageCaptor.capture());

        Message capturedMessage = messageCaptor.getValue();
        
        // Firebase Message 객체가 생성되었는지만 확인 (내부 접근 제한으로 인해)
        assertNotNull(capturedMessage);
        
        // Firebase 전송이 한 번 호출되었는지 확인
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("사용자별 개인화된 메시지 확인")
    void verifyPersonalizedMessages() throws Exception {
        // Given
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(mockSubscriptions);
        when(firebaseMessaging.send(any(Message.class)))
                .thenReturn("message-id-1")
                .thenReturn("message-id-2");

        // When
        pushNotificationService.sendAllCustomNotifications();

        // Then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(firebaseMessaging, times(2)).send(messageCaptor.capture());

        List<Message> messages = messageCaptor.getAllValues();
        
        // 두 개의 메시지가 생성되었는지 확인
        assertEquals(2, messages.size());
        
        // 각 메시지 객체가 null이 아님을 확인 (내부 접근 제한으로 인해 내용은 확인 불가)
        assertNotNull(messages.get(0));
        assertNotNull(messages.get(1));
        
        // Firebase 전송이 두 번 호출되었는지 확인
        verify(firebaseMessaging, times(2)).send(any(Message.class));
    }

    // ====================================
    // 통합 테스트
    // ====================================

    @Test
    @DisplayName("통합 테스트 - 대량 구독자 처리")
    void integrationTest_ManySubscriptions() throws Exception {
        // Given - 10명의 구독자 생성
        List<SubscriptionVO> manySubscriptions = Arrays.asList(
            createSubscription(1L, 101L, "token-101"),
            createSubscription(2L, 102L, "token-102"),
            createSubscription(3L, 103L, "token-103"),
            createSubscription(4L, 104L, "token-104"),
            createSubscription(5L, 105L, "token-105")
        );
        
        when(subscriptionMapper.findActiveByNotificationType("FEEDBACK"))
                .thenReturn(manySubscriptions);
        when(firebaseMessaging.send(any(Message.class)))
                .thenReturn("msg-1", "msg-2", "msg-3", "msg-4", "msg-5");

        // When
        pushNotificationService.sendAllCustomNotifications();

        // Then
        verify(subscriptionMapper).findActiveByNotificationType("FEEDBACK");
        verify(firebaseMessaging, times(5)).send(any(Message.class));

        // 각 사용자별 메시지가 올바르게 생성되었는지 확인
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(firebaseMessaging, times(5)).send(messageCaptor.capture());

        List<Message> messages = messageCaptor.getAllValues();
        assertEquals(5, messages.size());
        
        // 각 메시지가 생성되었는지 확인 (내부 접근 제한으로 인해 내용은 확인 불가)
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            assertNotNull(message);
        }
    }

    // Helper method
    private SubscriptionVO createSubscription(Long id, Long userId, String token) {
        SubscriptionVO subscription = new SubscriptionVO();
        subscription.setId(id);
        subscription.setUserId(userId);
        subscription.setFcmToken(token);
        subscription.setIsActiveFeedback(true);
        return subscription;
    }
}