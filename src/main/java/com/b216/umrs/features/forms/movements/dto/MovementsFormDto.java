package com.b216.umrs.features.forms.movements.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO для данных формы перемещений.
 * Содержит информацию о пользователе и его перемещениях.
 */
@Getter
@Setter
public class MovementsFormDto {
    private Integer age;
    private String gender;
    private String socialStatus;
    private List<Integer> transportationCosts;
    private AddressDto coordinatesAddress;
    private List<Integer> financialSituation;
    private String baseComment;
    private String dateMovements;
    private List<MovementItemDto> movements;
}

