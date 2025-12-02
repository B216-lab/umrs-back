package com.b216.umrs.features.auth.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class EmailMagicLinkHandler implements OneTimeTokenGenerationSuccessHandler {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    private final OneTimeTokenGenerationSuccessHandler redirectHandler =
        new RedirectOneTimeTokenGenerationSuccessHandler("/ott/check-email");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       OneTimeToken token) throws IOException, ServletException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(token.getUsername().trim());
        message.setSubject("Your one-time login code");
        message.setText("Your one-time login token: " + token.getTokenValue());

        mailSender.send(message);
        redirectHandler.handle(request, response, token);
    }
}
