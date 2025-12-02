package com.b216.umrs.features.auth.controller;

import com.b216.umrs.features.auth.dto.LoginRequest;
import com.b216.umrs.features.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для аутентификации пользователей через сессию.
 * Создаёт HTTP-сессию после успешной аутентификации.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public LoginController(
        AuthenticationManager authenticationManager,
        UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }
    
    @GetMapping("/token/one-time")
    public String sendOneTimeToken() {
        return "token-one-time";
    }

    /**
     * Выполняет вход пользователя и создаёт HTTP-сессию.
     *
     * @param request запрос с email и password
     * @return ResponseEntity с информацией об успешном входе
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        try {
            // Создание токена аутентификации
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            );

            // Аутентификация через AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Создаёт новый SecurityContext и сохраняет его как в текущий поток, так и в HTTP-сессию
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Явно создаёт/получает HTTP-сессию и сохраняет в неё SecurityContext,
            // чтобы сессия сохранялась между перезагрузками страницы
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
            );

            // Обновляет дату последнего входа пользователя
            userRepository.findByUsername(authentication.getName())
                .ifPresent(user -> {
                    user.setLastLogin(LocalDate.now());
                    userRepository.save(user);
                });

            log.info("User {} successfully authenticated", request.email());

            // Возврат информации о пользователе
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList());

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", request.email(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An error occurred during login"));
        }
    }

    /**
     * Получает информацию о текущем аутентифицированном пользователе.
     *
     * @return ResponseEntity с информацией о пользователе
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymous".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Not authenticated"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Выполняет выход пользователя и инвалидирует сессию.
     *
     * @return ResponseEntity с подтверждением выхода
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}

