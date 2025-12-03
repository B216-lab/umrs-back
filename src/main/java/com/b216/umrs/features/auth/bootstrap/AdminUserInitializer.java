package com.b216.umrs.features.auth.bootstrap;

import com.b216.umrs.features.auth.domain.Role;
import com.b216.umrs.features.auth.domain.User;
import com.b216.umrs.features.auth.repository.RoleRepository;
import com.b216.umrs.features.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminUserInitializer(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank()
            || adminPassword == null || adminPassword.isBlank()) {
            log.info("Admin user bootstrap skipped: app.admin.username or app.admin.password is not set");
            return;
        }

        Optional<User> existing = userRepository.findByUsername(adminUsername);
        if (existing.isPresent()) {
            log.info("Admin user '{}' already exists, bootstrap skipped", adminUsername);
            return;
        }

        Role adminRole = roleRepository.findByName(com.b216.umrs.features.auth.model.Role.ADMIN)
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found in database"));

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEnabled(true);
        admin.setLocked(false);
        admin.setRoles(List.of(adminRole));
        admin.setScopes(new ArrayList<>());

        userRepository.save(admin);
        log.info("Admin user '{}' has been created", adminUsername);
    }
}


