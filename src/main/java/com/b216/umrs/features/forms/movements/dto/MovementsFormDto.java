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
    private String birthday;
    private String gender;
    private String socialStatus;
    private Integer transportCostMin;
    private Integer transportCostMax;
    private AddressDto homeAddress;
    private Integer incomeMin;
    private Integer incomeMax;
    private String movementsDate;
    private List<MovementItemDto> movements;
}

