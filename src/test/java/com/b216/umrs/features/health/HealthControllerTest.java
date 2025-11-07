package com.b216.umrs.features.health;

import com.b216.umrs.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
@Import(SecurityConfig.class)
public class HealthControllerTest {

    @Autowired
    private MockMvc api;

    @Test
    @WithAnonymousUser
    void givenUserIsAnonymous_whenGetPublic_thenSuccess() throws Exception {
        api.perform(get("/api/health"))
            .andExpect(status().isOk());
    }

}
