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
    private String movementType;
    private List<String> transport;
    private Integer numberPeopleInCar;
    private Integer walkToStartMinutes;
    private Integer waitAtStartMinutes;
    private Integer numberOfTransfers;
    private String waitBetweenTransfersMinutes;
    private String departureTime;
    private String departurePlace;
    private AddressDto departureAddress;
    private String arrivalTime;
    private String arrivalPlace;
    private Integer walkFromFinishMinutes;
    private String tripCost;
    private AddressDto arrivalAddress;
    private String comment;
}

