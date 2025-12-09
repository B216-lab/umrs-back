package com.b216.umrs.features.movement.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;


@Entity
@Table(name = "movements")
@Getter
@Setter
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movement_type_id", nullable = false)
    private MovementTypeRef type;

    private OffsetDateTime departureTime;

    private OffsetDateTime destinationTime;

    private LocalDate day;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode departurePlace;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode destinationPlace;
    
    /**
     * Читаемый адрес отправления с минимумом данных, определяющих адрес, т.е. никаких почтовых индексов и тд.
     * Например: "г. Москва, ул. Ленина, д. 10"
     */
    @Column(name = "departure_place_address", length = 512)
    private String departurePlaceAddress;

    /**
     * Читаемый адрес назначения с минимумом данных, определяющих адрес, т.е. никаких почтовых индексов и тд.
     * Например: "г. Санкт-Петербург, Невский проспект, д. 50"
     */
    @Column(name = "destination_place_address", length = 512)
    private String destinationPlaceAddress;

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
