package com.b216.umrs.features.movement.domain;

import com.b216.umrs.infrastructure.persistence.JsonNodeStringConverter;
import com.b216.umrs.features.movement.domain.MovementTypeRef;
import com.b216.umrs.features.movement.domain.PlaceTypeRef;
import com.b216.umrs.features.movement.domain.VehicleTypeRef;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность перемещения.
 *
 * Поля:
 * - uuid: уникальный идентификатор записи
 * - type: тип перемещения (ref)
 * - departureTime: время отправления
 * - destinationTime: время прибытия
 * - day: календарная дата перемещения
 * - departurePlace: место отправления (GeoJSON)
 * - destinationPlace: место назначения (GeoJSON)
 * - departureType: тип места отправления (ref)
 * - destinationType: тип места назначения (ref)
 * - vehicleType: тип ТС (ref)
 * - cost: стоимость
 * - waitingTime: время ожидания (минуты)
 * - seatsAmount: количество мест
 */
@Entity
@Table(name = "movements")
@Getter
@Setter
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movement_type_id", nullable = false)
    private MovementTypeRef type;

    private OffsetDateTime departureTime;

    private OffsetDateTime destinationTime;

    private LocalDate day;

    /**
     * Геометрия хранится как JSONB (GeoJSON). Для пространственных операций возможно добавление маппинга в Geometry позднее.
     */
    @Column(columnDefinition = "jsonb")
    @jakarta.persistence.Convert(converter = JsonNodeStringConverter.class)
    private JsonNode departurePlace;

    @Column(columnDefinition = "jsonb")
    @jakarta.persistence.Convert(converter = JsonNodeStringConverter.class)
    private JsonNode destinationPlace;

    @ManyToOne(optional = false)
    @JoinColumn(name = "departure_place_type_id", nullable = false)
    private PlaceTypeRef departureType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_place_type_id", nullable = false)
    private PlaceTypeRef destinationType;

    @ManyToOne(optional = true)
    @JoinColumn(name = "vehicle_type_id")
    private VehicleTypeRef vehicleType;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    private Integer waitingTime;

    private Integer seatsAmount;

}


