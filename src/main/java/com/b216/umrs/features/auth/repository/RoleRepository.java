package com.b216.umrs.features.auth.repository;

import com.b216.umrs.features.auth.domain.RoleRef;
import com.b216.umrs.features.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для сущности RoleRef.
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleRef, Long> {
    Optional<RoleRef> findByName(Role name);
}

