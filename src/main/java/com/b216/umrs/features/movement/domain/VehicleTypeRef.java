package com.b216.umrs.features.movement.domain;

import com.b216.umrs.features.movement.model.VehicleType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Справочник типов транспортных средств.
 */
@Entity
@Table(name = "ref_vehicle_type")
@Getter
@Setter
public class VehicleTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 128)
    @Enumerated(EnumType.STRING)
    private VehicleType code;

    @Column(name = "description_ru", nullable = false, length = 512)
    private String descriptionRu;

}


