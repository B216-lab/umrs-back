package com.b216.umrs.features.movement.repository;

import com.b216.umrs.features.movement.domain.PlaceTypeRef;
import com.b216.umrs.features.movement.model.PlaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * REST-репозиторий для справочника типов мест.
 */
@RepositoryRestResource(path = "place-types", collectionResourceRel = "place-types")
public interface PlaceTypeRefRepository extends JpaRepository<PlaceTypeRef, Integer> {
    Optional<PlaceTypeRef> findByCode(PlaceType code);
}


