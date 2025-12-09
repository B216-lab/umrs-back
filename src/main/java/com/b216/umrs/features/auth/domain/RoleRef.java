package com.b216.umrs.features.auth.domain;

import com.b216.umrs.features.auth.model.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class RoleRef implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Role name;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_scopes", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "scope")
    private java.util.Set<String> scopes = new java.util.HashSet<>();
}

