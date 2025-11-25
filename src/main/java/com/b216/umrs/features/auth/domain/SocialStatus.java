package com.b216.umrs.features.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "social_statuses")
@Getter
@Setter
public class SocialStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 64)
    private com.b216.umrs.features.auth.model.SocialStatus code;

    @Column(name = "description_ru", nullable = false, length = 512)
    private String descriptionRu;
}
