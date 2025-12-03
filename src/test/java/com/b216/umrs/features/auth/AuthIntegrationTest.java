package com.b216.umrs.features.auth;

import com.b216.umrs.features.auth.util.TestUserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для аутентификации с использованием реальных пользователей в базе данных.
 * Пользователи создаются программно через TestUserFactory перед каждым тестом.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN_EMAIL = "ci_admin@test.local";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String MANAGER_EMAIL = "ci_manager@test.local";
    private static final String MANAGER_PASSWORD = "Manager123!";
    private static final String USER_EMAIL = "ci_user@test.local";
    private static final String USER_PASSWORD = "User12348!";

    @BeforeEach
    void setUp() {
        // Создаёт тестовых пользователей перед каждым тестом
        // Если пользователи уже существуют, они не будут пересозданы
        testUserFactory.ensureAdminUser(ADMIN_EMAIL, ADMIN_PASSWORD);
        testUserFactory.ensureManagerUser(MANAGER_EMAIL, MANAGER_PASSWORD);
        testUserFactory.ensureRegularUser(USER_EMAIL, USER_PASSWORD);
    }

    @Test
    void givenValidAdminCredentials_whenLogin_thenSuccess() throws Exception {
        Map<String, String> loginRequest = Map.of(
            "email", ADMIN_EMAIL,
            "password", ADMIN_PASSWORD
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.username").value(ADMIN_EMAIL))
            .andExpect(jsonPath("$.authorities").isArray());
    }

    @Test
    void givenValidManagerCredentials_whenLogin_thenSuccess() throws Exception {
        Map<String, String> loginRequest = Map.of(
            "email", MANAGER_EMAIL,
            "password", MANAGER_PASSWORD
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.username").value(MANAGER_EMAIL));
    }

    @Test
    void givenValidUserCredentials_whenLogin_thenSuccess() throws Exception {
        Map<String, String> loginRequest = Map.of(
            "email", USER_EMAIL,
            "password", USER_PASSWORD
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.username").value(USER_EMAIL));
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenUnauthorized() throws Exception {
        Map<String, String> loginRequest = Map.of(
            "email", ADMIN_EMAIL,
            "password", "wrong-password"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void givenNonExistentUser_whenLogin_thenUnauthorized() throws Exception {
        Map<String, String> loginRequest = Map.of(
            "email", "nonexistent@test.local",
            "password", "some-password"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }
}

