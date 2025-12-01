package com.b216.umrs.features.movement.web.v0;

import com.b216.umrs.features.movement.domain.Movement;
import com.b216.umrs.features.movement.model.MovementType;
import com.b216.umrs.features.movement.model.PlaceType;
import com.b216.umrs.features.movement.model.VehicleType;
import com.b216.umrs.features.movement.repository.MovementRepository;
import com.b216.umrs.features.movement.repository.MovementTypeRefRepository;
import com.b216.umrs.features.movement.repository.PlaceTypeRefRepository;
import com.b216.umrs.features.movement.repository.VehicleTypeRefRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * V1 контроллер для CRUD операций с Movement с упрощённой моделью DTO.
 */
@RestController
@RequestMapping("/api/v0/movements")
public class MovementV0Controller {

    private final MovementRepository movementRepository;
    private final MovementTypeRefRepository movementTypeRefRepository;
    private final PlaceTypeRefRepository placeTypeRefRepository;
    private final VehicleTypeRefRepository vehicleTypeRefRepository;

    public MovementV0Controller(
        MovementRepository movementRepository,
        MovementTypeRefRepository movementTypeRefRepository,
        PlaceTypeRefRepository placeTypeRefRepository,
        VehicleTypeRefRepository vehicleTypeRefRepository
    ) {
        this.movementRepository = movementRepository;
        this.movementTypeRefRepository = movementTypeRefRepository;
        this.placeTypeRefRepository = placeTypeRefRepository;
        this.vehicleTypeRefRepository = vehicleTypeRefRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovementDto> getById(@PathVariable("id") Long id) {
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

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovementDto> update(@PathVariable("id") Long id, @Valid @RequestBody MovementDto dto) {
        return movementRepository.findById(id)
            .map(existing -> {
                Movement updated = movementRepository.save(fromDto(dto, existing));
                return ResponseEntity.ok(toDto(updated));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!movementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        movementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MovementDto toDto(Movement m) {
        MovementDto dto = new MovementDto();
        dto.setId(m.getId());
        dto.setType(m.getType() != null && m.getType().getCode() != null ? m.getType().getCode().name() : null);
        dto.setDepartureTime(m.getDepartureTime());
        dto.setDestinationTime(m.getDestinationTime());
        dto.setDay(m.getDay());
        dto.setDeparturePlace(m.getDeparturePlace());
        dto.setDestinationPlace(m.getDestinationPlace());
        dto.setDepartureType(m.getDepartureType() != null && m.getDepartureType().getCode() != null ? m.getDepartureType().getCode().name() : null);
        dto.setDestinationType(m.getDestinationType() != null && m.getDestinationType().getCode() != null ? m.getDestinationType().getCode().name() : null);
        dto.setVehicleType(m.getVehicleType() != null && m.getVehicleType().getCode() != null ? m.getVehicleType().getCode().name() : null);
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
            try {
                MovementType enumValue = MovementType.valueOf(dto.getType());
                movementTypeRefRepository.findByCode(enumValue)
                    .ifPresentOrElse(
                        target::setType,
                        () -> {
                            throw new IllegalArgumentException("MovementType not found: " + dto.getType());
                        }
                    );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid MovementType: " + dto.getType(), e);
            }
        }
        if (dto.getDepartureType() != null) {
            try {
                PlaceType enumValue = PlaceType.valueOf(dto.getDepartureType());
                placeTypeRefRepository.findByCode(enumValue)
                    .ifPresentOrElse(
                        target::setDepartureType,
                        () -> {
                            throw new IllegalArgumentException("PlaceType not found: " + dto.getDepartureType());
                        }
                    );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid PlaceType: " + dto.getDepartureType(), e);
            }
        }
        if (dto.getDestinationType() != null) {
            try {
                PlaceType enumValue = PlaceType.valueOf(dto.getDestinationType());
                placeTypeRefRepository.findByCode(enumValue)
                    .ifPresentOrElse(
                        target::setDestinationType,
                        () -> {
                            throw new IllegalArgumentException("PlaceType not found: " + dto.getDestinationType());
                        }
                    );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid PlaceType: " + dto.getDestinationType(), e);
            }
        }
        if (dto.getVehicleType() != null) {
            try {
                VehicleType enumValue = VehicleType.valueOf(dto.getVehicleType());
                vehicleTypeRefRepository.findByCode(enumValue)
                    .ifPresentOrElse(
                        target::setVehicleType,
                        () -> {
                            throw new IllegalArgumentException("VehicleType not found: " + dto.getVehicleType());
                        }
                    );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid VehicleType: " + dto.getVehicleType(), e);
            }
        }
        return target;
    }
}
