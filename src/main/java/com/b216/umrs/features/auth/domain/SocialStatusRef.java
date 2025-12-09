package com.b216.umrs.features.auth.domain;

import com.b216.umrs.features.auth.model.SocialStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "social_statuses")
@Getter
@Setter
public class SocialStatusRef implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 64)
    private SocialStatus code;

    @Column(name = "description_ru", nullable = false, length = 512)
    private String descriptionRu;
}
