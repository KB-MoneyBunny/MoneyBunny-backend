package org.scoula.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailService 단위 테스트")
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // ====================================
    // 이메일 전송 테스트
    // ====================================

    @Test
    @DisplayName("이메일 전송 - 성공")
    void sendEmail_Success() {
        // Given
        String to = "test@example.com";
        String subject = "테스트 제목";
        String body = "테스트 본문";
        
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 - 빈 수신자")
    void sendEmail_EmptyRecipient() {
        // Given
        String to = "";
        String subject = "테스트 제목";
        String body = "테스트 본문";

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
    }

    @Test
    @DisplayName("이메일 전송 - null 수신자")
    void sendEmail_NullRecipient() {
        // Given
        String to = null;
        String subject = "테스트 제목";
        String body = "테스트 본문";

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
    }

    @Test
    @DisplayName("이메일 전송 - 긴 제목")
    void sendEmail_LongSubject() {
        // Given
        String to = "test@example.com";
        String subject = "매우 긴 제목 ".repeat(50); // 500자 이상
        String body = "테스트 본문";
        
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 - HTML 본문")
    void sendEmail_HtmlBody() {
        // Given
        String to = "test@example.com";
        String subject = "HTML 테스트";
        String body = "<html><body><h1>테스트</h1><p>HTML 본문입니다.</p></body></html>";
        
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    // 무시된 테스트 제거됨 - sendEmail_MultipleRecipients()

    // ====================================
    // 예외 처리 테스트
    // ====================================

    @Test
    @DisplayName("이메일 전송 - 전송 실패 (예외 발생)")
    void sendEmail_SendFailure() {
        // Given
        String to = "test@example.com";
        String subject = "테스트 제목";
        String body = "테스트 본문";
        
        doThrow(new RuntimeException("메일 서버 연결 실패"))
                .when(mailSender).send(any(MimeMessage.class));

        // When - 예외가 발생해도 메서드는 예외를 던지지 않음 (catch 처리)
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    // 무시된 테스트 제거됨 - sendEmail_CreateMessageFailure()

    @Test
    @DisplayName("이메일 전송 - 잘못된 이메일 형식")
    void sendEmail_InvalidEmailFormat() {
        // Given
        String to = "invalid-email-format";
        String subject = "테스트 제목";
        String body = "테스트 본문";

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
    }

    // ====================================
    // 특수 문자 처리 테스트
    // ====================================

    @Test
    @DisplayName("이메일 전송 - 특수 문자 포함")
    void sendEmail_WithSpecialCharacters() {
        // Given
        String to = "test@example.com";
        String subject = "특수문자 테스트: !@#$%^&*()";
        String body = "본문에 특수문자: 한글, 日本語, 中文, émoji 😀";
        
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 - 빈 제목과 본문")
    void sendEmail_EmptySubjectAndBody() {
        // Given
        String to = "test@example.com";
        String subject = "";
        String body = "";
        
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        assertDoesNotThrow(() -> mailService.sendEmail(to, subject, body));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    // ====================================
    // 통합 테스트
    // ====================================

    @Test
    @DisplayName("이메일 전송 - 연속 전송")
    void sendEmail_MultipleSends() {
        // Given
        String[] recipients = {"test1@example.com", "test2@example.com", "test3@example.com"};
        String subject = "연속 전송 테스트";
        String body = "테스트 본문";
        
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        for (String recipient : recipients) {
            assertDoesNotThrow(() -> mailService.sendEmail(recipient, subject, body));
        }

        // Then
        verify(mailSender, times(3)).createMimeMessage();
        verify(mailSender, times(3)).send(mimeMessage);
    }
}