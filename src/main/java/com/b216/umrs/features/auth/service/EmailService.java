package com.b216.umrs.features.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank() || subject == null) {
            log.warn("Некорректные параметры email: to={}, subject={}", to, subject);
            return;
        }
        if (!mailEnabled) {
            log.info("Почта отключена (app.mail.enabled=false): отправка email пропущена, to={}, subject={}", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body == null ? "" : body);

        mailSender.send(message);
        log.info("Письмо отправлено, to={}, subject={}", to, subject);
    }

    public void sendPlainText(String to, String subject, String body) {
        if (to == null || to.isBlank() || subject == null) {
            log.warn("Некорректные параметры email: to={}, subject={}", to, subject);
            return;
        }
        if (!mailEnabled) {
            log.info("Почта отключена (app.mail.enabled=false): отправка email пропущена, to={}, subject={}", to, subject);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body == null ? "" : body);
        mailSender.send(message);
    }

    public void sendHtml(String to, String subject, String htmlBody) throws MessagingException {
        if (to == null || to.isBlank() || subject == null) {
            log.warn("Некорректные параметры email: to={}, subject={}", to, subject);
            return;
        }
        if (!mailEnabled) {
            log.info("Почта отключена (app.mail.enabled=false): отправка HTML email пропущена, to={}, subject={}", to, subject);
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody == null ? "" : htmlBody, true); // true means this is HTML
        mailSender.send(message);
    }
}
