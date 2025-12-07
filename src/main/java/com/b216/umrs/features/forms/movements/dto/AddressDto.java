package com.b216.umrs.features.forms.movements.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для адреса из формы.
 * Упрощённый формат: содержит только читаемый адрес и координаты.
 */
@Getter
@Setter
public class AddressDto {
    /**
     * Читаемый адрес (текстовое представление).
     */
    private String value;
    
    /**
     * Широта (latitude).
     */
    private Double latitude;
    
    /**
     * Долгота (longitude).
     */
    private Double longitude;
}

