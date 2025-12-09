package com.b216.umrs.features.auth.domain;

import com.b216.umrs.features.auth.model.Scope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "scopes")
@Getter
@Setter
public class ScopeRef implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Scope name;
}

