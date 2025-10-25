package com.b216.umrs.features.movement.repository;

import com.b216.umrs.features.movement.domain.Movement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * REST-репозиторий для сущности Movement.
 */
@RepositoryRestResource(path = "movements", collectionResourceRel = "movements")
public interface MovementRepository extends JpaRepository<Movement, UUID> {
}


