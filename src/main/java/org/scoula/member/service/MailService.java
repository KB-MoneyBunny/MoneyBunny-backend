package org.scoula.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패", e);
        }
    }

}
