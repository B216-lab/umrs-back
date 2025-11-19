package com.b216.umrs.features.movement.domain;

import com.b216.umrs.infrastructure.persistence.JsonNodeStringConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


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
    @JoinColumn(name = "validation_status_id", nullable = false)
    private ValidationStatusRef validationStatus;

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


