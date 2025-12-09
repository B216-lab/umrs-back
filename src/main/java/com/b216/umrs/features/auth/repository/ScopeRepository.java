package com.b216.umrs.features.auth.repository;

import com.b216.umrs.features.auth.domain.ScopeRef;
import com.b216.umrs.features.auth.model.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для сущности ScopeRef.
 */
@Repository
public interface ScopeRepository extends JpaRepository<ScopeRef, Long> {
    Optional<ScopeRef> findByName(Scope name);
}

