package com.b216.umrs.features.forms.movements.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO для одного перемещения из формы.
 */
@Getter
@Setter
public class MovementItemDto {
    private String typeMovement;
    private List<String> transport;
    private Integer numberPeopleInCar;
    private Integer pedestrianApproachtoStartingStopOrParkingLot;
    private Integer waitingTimeForTransport;
    private Integer numberOfTransfers;
    private String waitingTimeBetweenTransfers;
    private String departureTime;
    private String departurePlace;
    private AddressDto coordinatesDepartureAddress;
    private String arrivalTime;
    private String arrivalPlace;
    private Integer pedestrianApproachFromFinalStopOrParking;
    private String number;
    private AddressDto coordinatesArrivalAddress;
    private String textarea;
}

