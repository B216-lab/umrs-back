package com.b216.umrs.features.auth.service;

import com.b216.umrs.features.auth.domain.CustomOneTimeToken;
import com.b216.umrs.features.auth.repository.CustomOneTimeTokenRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.ott.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOneTimeTokenService implements OneTimeTokenService {

    private final Clock clock = Clock.systemUTC();
    private final CustomOneTimeTokenRepository tokenRepository;

    public CustomOneTimeTokenService(CustomOneTimeTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public CustomOneTimeToken createTokenForUsername(String username) {
        String token = UUID.randomUUID().toString();
        Instant eightHoursFromNow = this.clock.instant().plus(8, ChronoUnit.HOURS);
        CustomOneTimeToken ott = new CustomOneTimeToken(token, username, eightHoursFromNow);
        tokenRepository.save(ott);
        return ott;
    }

    public CustomOneTimeToken consumeToken(String tokenValue) {
        Optional<CustomOneTimeToken> ott = tokenRepository.findByTokenValue(tokenValue);

        if (ott.isPresent()) {
            CustomOneTimeToken token = ott.get();
            if (!isExpired(token)) {
                tokenRepository.delete(token);
                return token;
            }
            tokenRepository.delete(token);
        }
        return null;
    }

    @Override
    @NonNull
    public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
        CustomOneTimeToken ott = createTokenForUsername(request.getUsername());
        return new DefaultOneTimeToken(ott.getTokenValue(), ott.getUsername(), ott.getExpiresAt());
    }

    @Override
    public OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
        CustomOneTimeToken token = consumeToken(authenticationToken.getTokenValue());
        if (token == null) {
            return null;
        }
        return new DefaultOneTimeToken(token.getTokenValue(), token.getUsername(), token.getExpiresAt());
    }

    private boolean isExpired(CustomOneTimeToken token) {
        return this.clock.instant().isAfter(token.getExpiresAt());
    }
}
