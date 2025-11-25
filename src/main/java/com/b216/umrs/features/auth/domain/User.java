package com.b216.umrs.features.auth.domain;

import com.b216.umrs.features.auth.model.Gender;
import com.b216.umrs.infrastructure.persistence.JsonNodeStringConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private Boolean enabled = true;

    private Boolean locked = false;

    private Integer maxSalary;

    private Integer minSalary;

    @Column(columnDefinition = "jsonb")
    @jakarta.persistence.Convert(converter = JsonNodeStringConverter.class)
    private JsonNode homePlace;

    private String homeReadablePlace;
    
    

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate lastLogin = LocalDate.now();

    private LocalDate creationDate = LocalDate.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_scopes",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "scope_id")
    )
    private List<Scope> scopes;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;
}
