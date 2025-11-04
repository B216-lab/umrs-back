package com.b216.umrs.config;

import com.b216.umrs.features.auth.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Базовая конфигурация безопасности: отключать аутентификацию и разрешать доступ ко всем эндпоинтам.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // TODO enable and configure
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/developers/**").hasRole(Role.DEVELOPER.name())
                .requestMatchers("/api/v1/managers/**").hasRole(Role.MANAGER.name())
                .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/api/v1/users/**").hasRole(Role.USER.name())
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v0/movements/").permitAll()
                .requestMatchers("/login").permitAll()
                .anyRequest().authenticated()
            )
            .logout(logoutConfigurer -> {
                logoutConfigurer.logoutUrl("/logout");
                logoutConfigurer.invalidateHttpSession(true);
            })
            .anonymous(anonymousConfigurer -> {
                anonymousConfigurer.principal("anonymous");
            })
            .exceptionHandling(exceptionHandlingConfigurer -> {
                exceptionHandlingConfigurer.accessDeniedPage("/access-denied");
            })
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService inMemoryUserDetails() {
        UserDetails userDetails = User.withUsername("admin")
            .password(passwordEncoder().encode("admin"))
            .roles(Role.ADMIN.name())
            .build();

        return new InMemoryUserDetailsManager(userDetails);
    }
}


