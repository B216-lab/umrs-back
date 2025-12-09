package com.b216.umrs.features.auth.controller;

import com.b216.umrs.features.auth.domain.CustomOneTimeToken;
import com.b216.umrs.features.auth.domain.RoleRef;
import com.b216.umrs.features.auth.domain.User;
import com.b216.umrs.features.auth.model.Role;
import com.b216.umrs.features.auth.repository.RoleRepository;
import com.b216.umrs.features.auth.repository.UserRepository;
import com.b216.umrs.features.auth.service.CustomOneTimeTokenService;
import com.b216.umrs.features.auth.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class OneTimeTokenAuthController {

    /**
     * DTO-запрос для генерации одноразового токена по email.
     *
     * :param email: email пользователя
     */
    public record OneTimeTokenRequest(String email) {
    }

    /**
     * DTO-ответ с временем истечения одноразового токена.
     *
     * :param expiresAt: момент времени, когда токен истекает
     */
    public record OneTimeTokenResponse(Instant expiresAt) {
    }

    /**
     * DTO-запрос для входа по одноразовому токену.
     *
     * :param token: строковое значение одноразового токена
     */
    public record OneTimeTokenLoginRequest(String token) {
    }

    private final CustomOneTimeTokenService oneTimeTokenService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public OneTimeTokenAuthController(
        CustomOneTimeTokenService oneTimeTokenService,
        UserDetailsService userDetailsService,
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        EmailService emailService
    ) {
        this.oneTimeTokenService = oneTimeTokenService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/ott")
    public ResponseEntity<OneTimeTokenResponse> generateOneTimeToken(
        @RequestBody OneTimeTokenRequest request
    ) {
        CustomOneTimeToken token = oneTimeTokenService.createTokenForUsername(request.email());

        // Отправить одноразовый токен на email пользователя
        this.emailService.sendPlainText(
            request.email().trim(),
            "Ваш одноразовый код входа",
            "Ваш одноразовый код: " + token.getTokenValue()
        );

        return ResponseEntity.ok(new OneTimeTokenResponse(token.getExpiresAt()));
    }

    @PostMapping("/submit-ott")
    public ResponseEntity<Map<String, Object>> loginWithOneTimeToken(
        @RequestBody OneTimeTokenLoginRequest request,
        HttpServletRequest httpRequest
    ) {
        CustomOneTimeToken token = oneTimeTokenService.consumeToken(request.token());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or expired token"));
        }

        String username = token.getUsername();

        // Создать пользователя, если email ранее не зарегистрирован
        User user = userRepository.findByUsername(username)
            .orElseGet(() -> createUserForEmail(username));

        // Обновляет дату последнего входа пользователя
        user.setLastLogin(LocalDate.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            securityContext
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Создать нового пользователя по email и назначить роль USER.
     *
     * :param email: email пользователя
     * :return: созданный пользователь
     */
    private User createUserForEmail(String email) {
        RoleRef userRole = roleRepository.findByName(Role.USER)
            .orElseThrow(() -> new IllegalStateException("Default USER role not found in database"));

        User newUser = new User();
        newUser.setUsername(email);
        // Генерировать случайный пароль, так как вход выполняется по одноразовому токену
        String randomPassword = UUID.randomUUID().toString();
        newUser.setPassword(passwordEncoder.encode(randomPassword));

        List<RoleRef> roles = new ArrayList<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        newUser.setScopes(new ArrayList<>());

        return userRepository.save(newUser);
    }
}


