package com.b216.umrs.features.movement.repository;

import com.b216.umrs.features.movement.domain.VehicleTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * REST-репозиторий для справочника типов транспортных средств.
 */
@RepositoryRestResource(path = "vehicle-types", collectionResourceRel = "vehicle-types")
public interface VehicleTypeRefRepository extends JpaRepository<VehicleTypeRef, Integer> {
}


