package com.b216.umrs.features.forms.movements.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для адреса из формы.
 * Содержит полную информацию об адресе из внешнего сервиса.
 */
@Getter
@Setter
public class AddressDto {
    private String value;
    private String unrestricted_value;
    private JsonNode data;
}

