package com.b216.umrs.features.forms.movements;

import com.b216.umrs.features.forms.movements.domain.InvalidRespondentKeyException;
import com.b216.umrs.features.forms.movements.dto.MovementsFormErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice(basePackageClasses = MovementsFormController.class)
public class MovementsFormExceptionHandler {

    @ExceptionHandler(InvalidRespondentKeyException.class)
    public ResponseEntity<MovementsFormErrorResponse> handleInvalidRespondentKey(InvalidRespondentKeyException exception) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Respondent key not found", exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<MovementsFormErrorResponse> handleValidationException(Exception exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
    }

    private ResponseEntity<MovementsFormErrorResponse> buildErrorResponse(
        HttpStatus status,
        String error,
        String message
    ) {
        return ResponseEntity.status(status).body(new MovementsFormErrorResponse(
            error,
            message,
            status.value(),
            OffsetDateTime.now()
        ));
    }
}
