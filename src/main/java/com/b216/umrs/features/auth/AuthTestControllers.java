package com.b216.umrs.features.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestControllers {

    @GetMapping("/employees")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String employeeTest() {
        return "You are an EMPLOYEE!";
    }

    @GetMapping("/managers")
    @PreAuthorize("hasRole('MANAGER')")
    public String managerTest() {
        return "You are a MANAGER!";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest() {
        return "You are an ADMIN!";
    }

    @GetMapping("/public")
    public String publicTest() {
        return "This endpoint is public.";
    }
}
