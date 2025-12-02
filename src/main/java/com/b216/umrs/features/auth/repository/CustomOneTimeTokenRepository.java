package com.b216.umrs.features.auth.repository;

import com.b216.umrs.features.auth.domain.CustomOneTimeToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CustomOneTimeTokenRepository extends JpaRepository<CustomOneTimeToken, Long> {
    Optional<CustomOneTimeToken> findByTokenValue(String tokenValue);

    @Modifying
    @Transactional
    @Query("DELETE FROM CustomOneTimeToken e WHERE e.expiresAt < :currentTimestamp")
    int deleteExpiredTokens(Instant currentTimestamp);
}
