package com.b216.umrs.features.movement.repository;

import com.b216.umrs.features.movement.domain.PlaceTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * REST-репозиторий для справочника типов мест.
 */
@RepositoryRestResource(path = "place-types", collectionResourceRel = "place-types")
public interface PlaceTypeRefRepository extends JpaRepository<PlaceTypeRef, Integer> {
}


