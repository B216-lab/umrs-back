package com.b216.umrs.features.movement.domain;

import com.b216.umrs.features.movement.model.ValidationStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ref_validation_status")
@Getter
@Setter
public class ValidationStatusRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 128)
    @Enumerated(EnumType.STRING)
    private ValidationStatus code;

    @Column(name = "description_ru", nullable = false, length = 512)
    private String descriptionRu;
}
