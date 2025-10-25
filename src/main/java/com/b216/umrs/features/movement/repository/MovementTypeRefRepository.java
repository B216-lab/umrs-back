package com.b216.umrs.features.movement.repository;

import com.b216.umrs.features.movement.domain.MovementTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * REST-репозиторий для справочника типов перемещений.
 */
@RepositoryRestResource(path = "movement-types", collectionResourceRel = "movement-types")
public interface MovementTypeRefRepository extends JpaRepository<MovementTypeRef, Integer> {
}


