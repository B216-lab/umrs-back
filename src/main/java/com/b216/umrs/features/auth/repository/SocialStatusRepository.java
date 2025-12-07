package com.b216.umrs.features.auth.repository;

import com.b216.umrs.features.auth.domain.SocialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для сущности SocialStatus.
 */
@Repository
public interface SocialStatusRepository extends JpaRepository<SocialStatus, Long> {
    /**
     * Находит социальный статус по коду.
     *
     * @param code код социального статуса (enum)
     * @return Optional с SocialStatus, если найден
     */
    Optional<SocialStatus> findByCode(com.b216.umrs.features.auth.model.SocialStatus code);
}

