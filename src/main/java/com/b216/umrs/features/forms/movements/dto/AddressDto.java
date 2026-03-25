package com.b216.umrs.features.forms.movements.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

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
     * GeoJSON адреса в формате Point.
     */
    @JsonAlias({"geojson", "geo_json"})
    private JsonNode geoJson;
    
    /**
     * Широта (latitude).
     */
    private Double latitude;
    
    /**
     * Долгота (longitude).
     */
    private Double longitude;
}

