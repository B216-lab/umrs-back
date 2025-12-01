package com.b216.umrs.features.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.ott.OneTimeToken;

import java.time.Instant;

@Entity
@Table(name = "one_time_tokens")
@Getter
@Setter
@NoArgsConstructor
public class CustomOneTimeToken implements OneTimeToken {

    public CustomOneTimeToken(String token, String username, Instant expireAt) {
        this.tokenValue = token;
        this.username = username;
        this.expiresAt = expireAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String tokenValue;

    @Column
    private Instant expiresAt;
}
