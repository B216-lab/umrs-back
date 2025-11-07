package com.b216.umrs.features.auth.repository;

import com.b216.umrs.features.auth.domain.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для сущности Scope.
 */
@Repository
public interface ScopeRepository extends JpaRepository<Scope, Long> {
    Optional<Scope> findByName(com.b216.umrs.features.auth.model.Scope name);
}

