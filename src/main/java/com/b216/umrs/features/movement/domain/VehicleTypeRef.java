package com.b216.umrs.features.movement.domain;

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
    private Integer id;

    @Column(nullable = false, unique = true, length = 128)
    private String code;

    @Column(name = "description_ru", nullable = false, length = 512)
    private String descriptionRu;

}


