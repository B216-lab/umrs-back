package com.b216.umrs.features.forms.movements.domain;

import com.b216.umrs.features.auth.domain.User;
import com.b216.umrs.features.auth.model.Gender;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Сущность для хранения данных формы перемещений.
 * Содержит информацию о пользователе из формы и ссылку на авторизованного пользователя (если есть).
 */
@Entity
@Table(name = "movements_form_submissions")
@Getter
@Setter
public class MovementsFormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на авторизованного пользователя (null для анонимных отправок).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * День рождения из формы.
     */
    private LocalDate birthday;

    /**
     * Пол из формы.
     */
    @Enumerated(EnumType.STRING)
    private Gender gender;

    /**
     * Социальный статус из формы.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_status_id")
    private com.b216.umrs.features.auth.domain.SocialStatusRef socialStatus;

    /**
     * Минимальные расходы на транспорт.
     */
    @Column(name = "transport_cost_min")
    private Integer transportCostMin;

    /**
     * Максимальные расходы на транспорт.
     */
    @Column(name = "transport_cost_max")
    private Integer transportCostMax;

    /**
     * Минимальный доход.
     */
    @Column(name = "income_min")
    private Integer incomeMin;

    /**
     * Максимальный доход.
     */
    @Column(name = "income_max")
    private Integer incomeMax;

    /**
     * Домашний адрес в формате GeoJSON (JSONB).
     */
    @Column(name = "home_address", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode homeAddress;

    /**
     * Читаемый домашний адрес.
     */
    @Column(name = "home_readable_address", length = 512)
    private String homeReadableAddress;

    /**
     * Дата передвижений из формы.
     */
    @Column(name = "movements_date")
    private LocalDate movementsDate;

    /**
     * Время создания записи.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Устанавливает время создания перед сохранением в базу данных.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
