package com.b216.umrs.features.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class AuthTestControllers {

    @GetMapping("/developer")
    public String developerTest() {
        return "You are an DEVELOPER!";
    }

    @GetMapping("/manager")
    public String managerTest() {
        return "You are a MANAGER!";
    }

    @GetMapping("/user")
    public String userTest() {
        return "You are an USER!";
    }

    @GetMapping("/admin")
    public String adminTest() {
        return "You are an ADMIN!";
    }

    @GetMapping("/public")
    public String publicTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("AuthenticatedUser: {}", authentication.getName());
        authentication.getAuthorities().forEach(authority -> {
            log.info("GrantedAuthority: {}", authority);
        });
        return "This endpoint is public.";
    }
}
