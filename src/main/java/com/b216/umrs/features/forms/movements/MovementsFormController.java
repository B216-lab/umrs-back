package com.b216.umrs.features.forms.movements;

import com.b216.umrs.features.forms.movements.dto.MovementsFormDto;
import com.b216.umrs.features.forms.movements.service.MovementsFormService;
import com.b216.umrs.features.movement.domain.Movement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/forms/movements")
public class MovementsFormController {

    private final MovementsFormService movementsFormService;

    public MovementsFormController(MovementsFormService movementsFormService) {
        this.movementsFormService = movementsFormService;
    }

    /**
     * Принимает данные формы перемещений, обрабатывает их и сохраняет в базу данных.
     *
     * @param formDto данные формы перемещений
     * @return ResponseEntity с сообщением об успешной обработке и количеством сохранённых перемещений
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitForm(@RequestBody MovementsFormDto formDto) {
        List<Movement> savedMovements = movementsFormService.processForm(formDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Данные формы успешно обработаны и сохранены",
            "savedMovementsCount", savedMovements.size()
        ));
    }
}
