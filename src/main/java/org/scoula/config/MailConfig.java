package org.scoula.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttls;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.debug", "true");

        return sender;
    }
}
