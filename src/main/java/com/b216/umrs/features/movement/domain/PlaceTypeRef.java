package com.b216.umrs.features.movement.domain;

import com.b216.umrs.features.movement.model.PlaceType;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Справочник типов мест.
 */
@Entity
@Table(name = "ref_place_type")
@Getter
@Setter
public class PlaceTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    @Enumerated(EnumType.STRING)
    private PlaceType code;

    @Column(name = "description_ru", nullable = false, length = 512)
    private String descriptionRu;
}
