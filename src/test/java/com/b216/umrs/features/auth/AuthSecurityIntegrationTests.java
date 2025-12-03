package com.b216.umrs.features.auth;

import com.b216.umrs.config.SecurityConfig;
import com.b216.umrs.features.auth.controller.AuthTestControllers;
import com.b216.umrs.features.auth.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthTestControllers.class)
@Import(SecurityConfig.class)
class AuthSecurityIntegrationTests {

    @Autowired
    private MockMvc api;


    @MockBean
    private UserDetailsService userDetailsService;

    private final String adminUsername = "power_admin@local.dev";
    private final String endpointPrefix = "/api/v1";

    @Test
    @WithAnonymousUser
    void givenUserIsAnonymous_whenGetAdmin_thenUnauthorized() throws Exception {
        api.perform(get("/api/v1/admin"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void givenUserIsAnonymous_whenGetPublic_thenSuccess() throws Exception {
        api.perform(get("/api/v1/public"))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/manager", "/developer"})
    void givenUserHasAdminRole_whenGetAnyEndpoint_thenSuccess(String endpoint) throws Exception {
        var adminUser = user(this.adminUsername)
            .roles(Role.ADMIN.name());

        api.perform(get(this.endpointPrefix + endpoint).with(adminUser))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(
        value = Role.class,
        names = {"USER", "MANAGER", "DEVELOPER", "ADMIN"}
    )
    void givenUserWithAnyRole_whenGetUser_thenSuccess(Role role) throws Exception {
        var mockUser = user(role.name().toLowerCase() + "@local.dev")
            .roles(role.name());
        api.perform(get(this.endpointPrefix + "/user").with(mockUser))
            .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void givenUserIsAnonymous_whenGetUser_thenUnauthorized() throws Exception {
        api.perform(get(this.endpointPrefix + "/user"))
            .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(
        value = Role.class,
        names = {"USER", "MANAGER", "DEVELOPER"}
    )
    void givenNonAdminUser_whenGetAdmin_thenForbidden(Role role) throws Exception {
        var mockUser = user(role.name().toLowerCase() + "@local.dev")
            .roles(role.name());

        api.perform(get(this.endpointPrefix + "/admin").with(mockUser))
            .andExpect(status().isForbidden());
    }
}
