package com.b216.umrs.features.auth.controller;

import com.b216.umrs.features.auth.domain.CustomOneTimeToken;
import com.b216.umrs.features.auth.service.CustomOneTimeTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class OneTimeTokenAuthController {

    public record OneTimeTokenRequest(String email) {
    }

    public record OneTimeTokenResponse(String token, Instant expiresAt) {
    }

    public record OneTimeTokenLoginRequest(String token) {
    }

    private final CustomOneTimeTokenService oneTimeTokenService;
    private final UserDetailsService userDetailsService;

    public OneTimeTokenAuthController(CustomOneTimeTokenService oneTimeTokenService,
                                      UserDetailsService userDetailsService) {
        this.oneTimeTokenService = oneTimeTokenService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/ott")
    public ResponseEntity<OneTimeTokenResponse> generateOneTimeToken(
        @RequestBody OneTimeTokenRequest request
    ) {
        CustomOneTimeToken token = oneTimeTokenService.createTokenForUsername(request.email());
        return ResponseEntity.ok(new OneTimeTokenResponse(token.getTokenValue(), token.getExpiresAt()));
    }

    @PostMapping("/submit-ott")
    public ResponseEntity<Map<String, Object>> loginWithOneTimeToken(
        @RequestBody OneTimeTokenLoginRequest request,
        HttpServletRequest httpRequest
    ) {
        CustomOneTimeToken token = oneTimeTokenService.consumeToken(request.token());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or expired token"));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUsername());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            securityContext
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .toList());

        return ResponseEntity.ok(response);
    }
}



