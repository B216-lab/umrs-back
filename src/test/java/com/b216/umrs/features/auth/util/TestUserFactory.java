package com.b216.umrs.features.auth.util;

import com.b216.umrs.features.auth.domain.RoleRef;
import com.b216.umrs.features.auth.domain.ScopeRef;
import com.b216.umrs.features.auth.domain.User;
import com.b216.umrs.features.auth.repository.RoleRepository;
import com.b216.umrs.features.auth.repository.ScopeRepository;
import com.b216.umrs.features.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TestUserFactory {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ScopeRepository scopeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TestUserFactory(
        UserRepository userRepository,
        RoleRepository roleRepository,
        ScopeRepository scopeRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.scopeRepository = scopeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Создаёт пользователя с указанными ролями, если он ещё не существует.
     *
     * :param username: имя пользователя (email)
     * :param password: пароль в открытом виде (будет закодирован)
     * :param roleEnums: список ролей для назначения пользователю
     * :return: созданный или существующий пользователь
     */
    public User ensureUserWithRoles(String username, String password, com.b216.umrs.features.auth.model.Role... roleEnums) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        User user = existingUser.orElseGet(User::new);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setLocked(false);

        List<RoleRef> roles = new ArrayList<>();
        for (com.b216.umrs.features.auth.model.Role roleEnum : roleEnums) {
            RoleRef role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new IllegalStateException(
                    "Role " + roleEnum + " not found in database. Ensure migrations are applied."));
            roles.add(role);
        }
        user.setRoles(roles);
        user.setScopes(new ArrayList<>());

        return userRepository.save(user);
    }

    /**
     * Создаёт пользователя с указанными ролями и правами, если он ещё не существует.
     *
     * :param username: имя пользователя (email)
     * :param password: пароль в открытом виде (будет закодирован)
     * :param roleEnums: список ролей для назначения пользователю
     * :param scopeEnums: список прав для назначения пользователю
     * :return: созданный или существующий пользователь
     */
    public User ensureUserWithRolesAndScopes(
        String username,
        String password,
        List<com.b216.umrs.features.auth.model.Role> roleEnums,
        List<com.b216.umrs.features.auth.model.Scope> scopeEnums
    ) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setLocked(false);

        List<RoleRef> roles = new ArrayList<>();
        for (com.b216.umrs.features.auth.model.Role roleEnum : roleEnums) {
            RoleRef role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new IllegalStateException(
                    "Role " + roleEnum + " not found in database. Ensure migrations are applied."));
            roles.add(role);
        }
        user.setRoles(roles);

        List<ScopeRef> scopes = new ArrayList<>();
        for (com.b216.umrs.features.auth.model.Scope scopeEnum : scopeEnums) {
            ScopeRef scope = scopeRepository.findByName(scopeEnum)
                .orElseThrow(() -> new IllegalStateException(
                    "Scope " + scopeEnum + " not found in database. Ensure migrations are applied."));
            scopes.add(scope);
        }
        user.setScopes(scopes);

        return userRepository.save(user);
    }

    /**
     * Создаёт пользователя с ролью ADMIN, если он ещё не существует.
     *
     * :param username: имя пользователя (email)
     * :param password: пароль в открытом виде (будет закодирован)
     * :return: созданный или существующий пользователь
     */
    public User ensureAdminUser(String username, String password) {
        return ensureUserWithRoles(username, password, com.b216.umrs.features.auth.model.Role.ADMIN);
    }

    /**
     * Создаёт пользователя с ролью MANAGER, если он ещё не существует.
     *
     * :param username: имя пользователя (email)
     * :param password: пароль в открытом виде (будет закодирован)
     * :return: созданный или существующий пользователь
     */
    public User ensureManagerUser(String username, String password) {
        return ensureUserWithRoles(username, password, com.b216.umrs.features.auth.model.Role.MANAGER);
    }

    /**
     * Создаёт пользователя с ролью USER, если он ещё не существует.
     *
     * :param username: имя пользователя (email)
     * :param password: пароль в открытом виде (будет закодирован)
     * :return: созданный или существующий пользователь
     */
    public User ensureRegularUser(String username, String password) {
        return ensureUserWithRoles(username, password, com.b216.umrs.features.auth.model.Role.USER);
    }

    /**
     * Создаёт пользователя с ролью DEVELOPER, если он ещё не существует.
     *
     * :param username: имя пользователя (email)
     * :param password: пароль в открытом виде (будет закодирован)
     * :return: созданный или существующий пользователь
     */
    public User ensureDeveloperUser(String username, String password) {
        return ensureUserWithRoles(username, password, com.b216.umrs.features.auth.model.Role.DEVELOPER);
    }

    /**
     * Удаляет пользователя по имени пользователя (email), если он существует.
     *
     * :param username: имя пользователя (email)
     */
    public void deleteUser(String username) {
        userRepository.findByUsername(username)
            .ifPresent(userRepository::delete);
    }
}

