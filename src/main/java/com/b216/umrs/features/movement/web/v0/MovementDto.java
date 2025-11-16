package com.b216.umrs.features.movement.web.v0;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class MovementDto {
    private UUID id;
    private String type;
    private OffsetDateTime departureTime;
    private OffsetDateTime destinationTime;
    private LocalDate day;
    private com.fasterxml.jackson.databind.JsonNode departurePlace;
    private com.fasterxml.jackson.databind.JsonNode destinationPlace;
    private String departureType;
    private String destinationType;
    private String vehicleType;
    private java.math.BigDecimal cost;
    private Integer waitingTime;
    private Integer seatsAmount;
}
