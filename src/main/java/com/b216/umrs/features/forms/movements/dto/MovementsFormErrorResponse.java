package com.b216.umrs.features.forms.movements.dto;

import java.time.OffsetDateTime;

public record MovementsFormErrorResponse(
    String error,
    String message,
    int status,
    OffsetDateTime timestamp
) {
}
