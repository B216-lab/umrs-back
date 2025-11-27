package com.b216.umrs.config;

import com.b216.umrs.features.auth.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Базовая конфигурация безопасности: отключать аутентификацию и разрешать доступ ко всем эндпоинтам.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(httpSecuritySessionManagementConfigurer -> {
                httpSecuritySessionManagementConfigurer.maximumSessions(15)
                    .maxSessionsPreventsLogin(true)
                    .expiredUrl("/login?expired=true");
            })
            .csrf(csrf -> csrf.disable()) // TODO enable and configure
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/developer/**").hasAnyRole(Role.DEVELOPER.name(), Role.ADMIN.name())
                .requestMatchers("/api/v1/manager/**").hasAnyRole(Role.MANAGER.name(), Role.ADMIN.name())
                .requestMatchers("/api/v1/user/**").hasAnyRole(Role.USER.name(), Role.DEVELOPER.name(), Role.MANAGER.name(), Role.ADMIN.name())
                .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/signup/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/v1/public/forms/**").permitAll()
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
            .httpBasic(basic -> {
            })
            .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Разрешает запросы с любых источников (для разработки)
        // TODO В продакшене указать конкретные домены
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false); // Должно быть false при allowedOrigins("*")
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


