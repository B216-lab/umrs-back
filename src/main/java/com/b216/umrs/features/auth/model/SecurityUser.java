package com.b216.umrs.features.auth.model;

import com.b216.umrs.features.auth.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record SecurityUser(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert user's roles and scopes to List of SimpleGrantedAuthority
        return Stream.concat(
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(
                        "ROLE_" + role.getName())),
                user.getScopes().stream()
                    .map(scope -> new SimpleGrantedAuthority(
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
