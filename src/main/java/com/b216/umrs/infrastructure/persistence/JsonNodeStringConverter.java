package com.b216.umrs.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Конвертер JsonNode <-> String для хранения в колонке JSONB.
 */
@Converter(autoApply = false)
public class JsonNodeStringConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Невозможно сериализовать JsonNode", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(dbData);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Невозможно десериализовать JsonNode", e);
        }
    }
}


