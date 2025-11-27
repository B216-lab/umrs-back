package com.b216.umrs.features.movement.repository;

import com.b216.umrs.features.movement.domain.ValidationStatusRef;
import com.b216.umrs.features.movement.model.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * REST-репозиторий для справочника статусов валидации.
 */
@RepositoryRestResource(path = "validation-statuses", collectionResourceRel = "validation-statuses")
public interface ValidationStatusRefRepository extends JpaRepository<ValidationStatusRef, Long> {
    Optional<ValidationStatusRef> findByCode(ValidationStatus code);
}

