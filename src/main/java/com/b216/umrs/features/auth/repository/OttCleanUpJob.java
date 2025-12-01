package com.b216.umrs.features.auth.repository;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class OttCleanUpJob {

    private final CustomOneTimeTokenRepository tokenRepository;

    public OttCleanUpJob(CustomOneTimeTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 hour
    @Transactional
    public void cleanUpOldRecords() {
        log.info("Deleting expired tokens...");
        int deleted = tokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Deleted {} expired tokens", deleted);
    }
}
