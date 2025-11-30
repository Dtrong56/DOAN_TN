package com.example.notification_service.service;

import com.example.notification_service.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary   // ưu tiên dùng SMTP thay vì mock
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false); // false = plaintext, true = HTML

            mailSender.send(message);

            log.info("EMAIL SENT (REAL) → To: {}, Subject: {}", to, subject);

        } catch (MessagingException ex) {
            log.error("FAILED TO SEND EMAIL → To: {}, ERROR: {}", to, ex.getMessage());
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}
