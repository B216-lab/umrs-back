package com.b216.umrs.features.movement.domain;

import com.b216.umrs.features.movement.model.MovementType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Справочник типов перемещений.
 */
@Entity
@Table(name = "ref_movement_type")
@Getter
@Setter
public class MovementTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 64)
    @Enumerated(EnumType.STRING)
    private MovementType code;

    @Column(name = "description_ru", nullable = false, length = 256)
    private String descriptionRu;
}


