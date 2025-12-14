package com.b216.umrs.features.auth.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class CsrfController {

    /**
     * Возвращать текущий CSRF-токен для SPA-клиента.
     *
     * :param csrfToken: объект токена, предоставляемый Spring Security
     * :return: карта с именем заголовка и значением токена
     */
    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(@Nullable CsrfToken csrfToken) {
        if (csrfToken == null) {
            // CSRF отключён (публичный режим), токен не требуется.
            return Map.of(
                "headerName", "X-XSRF-TOKEN",
                "token", ""
            );
        }
        return Map.of(
            "headerName", csrfToken.getHeaderName(),
            "token", csrfToken.getToken()
        );
    }
}


