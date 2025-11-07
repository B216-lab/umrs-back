package com.b216.umrs.features.auth.controller;

import com.b216.umrs.features.auth.domain.User;
import com.b216.umrs.features.auth.dto.SignupRequest;
import com.b216.umrs.features.auth.domain.Role;
import com.b216.umrs.features.auth.repository.RoleRepository;
import com.b216.umrs.features.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для регистрации пользователей.
 */
@RestController
@RequestMapping("/api/v1")
public class SignupController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupController(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Проверяет существование пользователя с указанным именем (email).
     *
     * @param request запрос с email и password
     * @return ResponseEntity с информацией о существовании пользователя
     */
    @PostMapping("/signup/check")
    public ResponseEntity<String> checkUserExists(@Valid @RequestBody SignupRequest request) {
        boolean userExists = userRepository.findByUsername(request.email()).isPresent();

        if (userExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("User with username " + request.email() + " already exists");
        }

        return ResponseEntity.ok("User with username " + request.email() + " does not exist");
    }

    /**
     * Создает нового пользователя с указанными данными.
     *
     * @param request запрос с email и password
     * @return ResponseEntity с результатом регистрации
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        // Проверка существования пользователя
        if (userRepository.findByUsername(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("User with username " + request.email() + " already exists");
        }

        // Получение роли USER по умолчанию
        Role userRole = roleRepository.findByName(com.b216.umrs.features.auth.model.Role.USER)
            .orElseThrow(() -> new RuntimeException("Default USER role not found in database"));

        // Создание нового пользователя
        User newUser = new User();
        newUser.setUsername(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));

        // Назначение роли USER
        List<Role> roles = new ArrayList<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        newUser.setScopes(new ArrayList<>());

        // Сохранение пользователя
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body("User " + request.email() + " successfully registered");
    }
}
