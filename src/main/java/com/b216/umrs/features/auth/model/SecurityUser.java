package com.b216.umrs.features.auth.model;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.b216.umrs.features.auth.domain.User;

public class SecurityUser implements UserDetails {

    private User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert user's roles and scopes to List of SimpleGrantedAuthority
        return java.util.stream.Stream.concat(
                user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + role.getName())),
                user.getScopes().stream()
                        .map(scope -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "SCOPE_" + scope.getName())))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }
}
