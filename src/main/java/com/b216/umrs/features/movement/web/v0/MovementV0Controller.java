package com.b216.umrs.features.movement.web.v0;

import com.b216.umrs.features.movement.domain.Movement;
import com.b216.umrs.features.movement.domain.MovementTypeRef;
import com.b216.umrs.features.movement.domain.PlaceTypeRef;
import com.b216.umrs.features.movement.domain.VehicleTypeRef;
import com.b216.umrs.features.movement.repository.MovementRepository;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * V1 контроллер для CRUD операций с Movement с упрощённой моделью DTO.
 */
@RestController
@RequestMapping("/api/v0/movements")
public class MovementV0Controller {

    private final MovementRepository movementRepository;

    public MovementV0Controller(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovementDto> getById(@PathVariable("id") UUID id) {
        Optional<Movement> found = movementRepository.findById(id);
        return found.map(m -> ResponseEntity.ok(toDto(m)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MovementDto> create(@Valid @RequestBody MovementDto dto) {
        Movement saved = movementRepository.save(fromDto(dto, new Movement()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping
    public ResponseEntity<java.util.List<MovementDto>> getAll() {
        java.util.List<MovementDto> result = movementRepository.findAll()
            .stream()
            .map(this::toDto)
            .toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovementDto> update(@PathVariable("id") UUID id, @Valid @RequestBody MovementDto dto) {
        return movementRepository.findById(id)
            .map(existing -> {
                Movement updated = movementRepository.save(fromDto(dto, existing));
                return ResponseEntity.ok(toDto(updated));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        if (!movementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        movementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MovementDto toDto(Movement m) {
        MovementDto dto = new MovementDto();
        dto.setId(m.getUuid());
        dto.setType(m.getType() != null ? m.getType().getCode() : null);
        dto.setDepartureTime(m.getDepartureTime());
        dto.setDestinationTime(m.getDestinationTime());
        dto.setDay(m.getDay());
        dto.setDeparturePlace(m.getDeparturePlace());
        dto.setDestinationPlace(m.getDestinationPlace());
        dto.setDepartureType(m.getDepartureType() != null ? m.getDepartureType().getCode() : null);
        dto.setDestinationType(m.getDestinationType() != null ? m.getDestinationType().getCode() : null);
        dto.setVehicleType(m.getVehicleType() != null ? m.getVehicleType().getCode() : null);
        dto.setCost(m.getCost());
        dto.setWaitingTime(m.getWaitingTime());
        dto.setSeatsAmount(m.getSeatsAmount());
        return dto;
    }

    private Movement fromDto(MovementDto dto, Movement target) {
        target.setDepartureTime(dto.getDepartureTime());
        target.setDestinationTime(dto.getDestinationTime());
        target.setDay(dto.getDay());
        target.setDeparturePlace(dto.getDeparturePlace());
        target.setDestinationPlace(dto.getDestinationPlace());
        target.setCost(dto.getCost());
        target.setWaitingTime(dto.getWaitingTime());
        target.setSeatsAmount(dto.getSeatsAmount());

        if (dto.getType() != null) {
            MovementTypeRef ref = new MovementTypeRef();
            ref.setCode(dto.getType());
            target.setType(ref);
        }
        if (dto.getDepartureType() != null) {
            PlaceTypeRef ref = new PlaceTypeRef();
            ref.setCode(dto.getDepartureType());
            target.setDepartureType(ref);
        }
        if (dto.getDestinationType() != null) {
            PlaceTypeRef ref = new PlaceTypeRef();
            ref.setCode(dto.getDestinationType());
            target.setDestinationType(ref);
        }
        if (dto.getVehicleType() != null) {
            VehicleTypeRef ref = new VehicleTypeRef();
            ref.setCode(dto.getVehicleType());
            target.setVehicleType(ref);
        }
        return target;
    }

    @Getter
    @Setter
    public static class MovementDto {
        private UUID id;
        private String type;
        private OffsetDateTime departureTime;
        private OffsetDateTime destinationTime;
        private LocalDate day;
        private com.fasterxml.jackson.databind.JsonNode departurePlace;
        private com.fasterxml.jackson.databind.JsonNode destinationPlace;
        private String departureType;
        private String destinationType;
        private String vehicleType;
        private java.math.BigDecimal cost;
        private Integer waitingTime;
        private Integer seatsAmount;
    }
}


