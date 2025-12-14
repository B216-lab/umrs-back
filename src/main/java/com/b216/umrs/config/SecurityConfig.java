package com.b216.umrs.config;

import com.b216.umrs.features.auth.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Разрешённые origins для CORS.
     * Может быть задано через переменную окружения CORS_ALLOWED_ORIGINS (через запятую).
     * По умолчанию используются значения для разработки.
     */
    @Value("${cors.allowed.origins:http://localhost:5173,http://localhost:3000,http://manual-geoform:5173,http://manual-geoform:80,http://frontend:5173,http://frontend:80}")
    private String corsAllowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.maximumSessions(15)
                    .maxSessionsPreventsLogin(true)
            )
            .csrf(csrf -> csrf
                // Use cookie-based CSRF tokens that are readable from JavaScript (for SPA clients)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Public forms endpoints are consumed without CSRF tokens (e.g. embedded Vueform),
                // so CSRF protection is not applied there
                .ignoringRequestMatchers("/api/v1/public/forms/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/developer/**").hasAnyRole(Role.DEVELOPER.name(), Role.ADMIN.name())
                .requestMatchers("/api/v1/manager/**").hasAnyRole(Role.MANAGER.name(), Role.ADMIN.name())
                .requestMatchers("/api/v1/user/**").hasAnyRole(Role.USER.name(), Role.DEVELOPER.name(), Role.MANAGER.name(), Role.ADMIN.name())
                .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/signup/**").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/logout").permitAll()
                .requestMatchers("/api/v1/auth/ott").permitAll()
                .requestMatchers("/api/v1/auth/submit-ott").permitAll()
                .requestMatchers("/api/v1/auth/me").authenticated()
                .requestMatchers("/api/v1/auth/csrf").permitAll()
                .requestMatchers("/actuator/health").permitAll()
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
                exceptionHandlingConfigurer
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler());
            })
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Парсим разрешённые origins из переменной окружения (разделённые запятой)
        List<String> allowedOrigins = parseAllowedOrigins(corsAllowedOrigins);
        configuration.setAllowedOrigins(allowedOrigins);
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Включает отправку cookies (JSESSIONID)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Парсит строку с разрешёнными origins (разделённые запятой) в список.
     * Удаляет пробелы и пустые значения.
     *
     * @param originsStr строка с origins, разделёнными запятой
     * @return список origins
     */
    private List<String> parseAllowedOrigins(String originsStr) {
        if (originsStr == null || originsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(originsStr.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    /**
     * Returns 401 Unauthorized response body with error message when authentication is required.
     *
     * @return
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Unauthorized");
            body.put("message", authException.getMessage());
            body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        };
    }

    /**
     * Returns 403 Forbidden response body with error message when access is denied.
     *
     * @return
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Forbidden");
            body.put("message", accessDeniedException.getMessage());
            body.put("status", HttpServletResponse.SC_FORBIDDEN);
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        };
    }
}


