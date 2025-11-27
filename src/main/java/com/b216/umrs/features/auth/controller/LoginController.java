package com.b216.umrs.features.auth.controller;

import com.b216.umrs.features.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    public LoginController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Выполняет вход пользователя и создаёт HTTP-сессию.
     *
     * @param request запрос с email и password
     * @return ResponseEntity с информацией об успешном входе
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Создание токена аутентификации
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            );

            // Аутентификация через AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Сохранение аутентификации в SecurityContext (и в сессии)
            SecurityContextHolder.getContext().setAuthentication(authentication);

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

