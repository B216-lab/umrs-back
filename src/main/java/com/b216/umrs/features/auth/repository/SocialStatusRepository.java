package com.b216.umrs.features.auth.repository;

import com.b216.umrs.features.auth.domain.SocialStatusRef;
import com.b216.umrs.features.auth.model.SocialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для сущности SocialStatusRef.
 */
@Repository
public interface SocialStatusRepository extends JpaRepository<SocialStatusRef, Long> {
    /**
     * Находит социальный статус по коду.
     *
     * @param code код социального статуса (enum)
     * @return Optional с SocialStatusRef, если найден
     */
    Optional<SocialStatusRef> findByCode(SocialStatus code);
}
