package com.b216.umrs.features.forms.movements.service;

import com.b216.umrs.features.forms.movements.dto.AddressDto;
import com.b216.umrs.features.forms.movements.dto.MovementItemDto;
import com.b216.umrs.features.forms.movements.dto.MovementsFormDto;
import com.b216.umrs.features.movement.domain.Movement;
import com.b216.umrs.features.movement.domain.MovementTypeRef;
import com.b216.umrs.features.movement.domain.PlaceTypeRef;
import com.b216.umrs.features.movement.domain.ValidationStatusRef;
import com.b216.umrs.features.movement.model.MovementType;
import com.b216.umrs.features.movement.model.PlaceType;
import com.b216.umrs.features.movement.model.ValidationStatus;
import com.b216.umrs.features.movement.model.VehicleType;
import com.b216.umrs.features.movement.repository.MovementRepository;
import com.b216.umrs.features.movement.repository.MovementTypeRefRepository;
import com.b216.umrs.features.movement.repository.PlaceTypeRefRepository;
import com.b216.umrs.features.movement.repository.ValidationStatusRefRepository;
import com.b216.umrs.features.movement.repository.VehicleTypeRefRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для обработки данных формы перемещений.
 * Преобразует DTO в сущности и сохраняет их в базу данных.
 */
@Service
public class MovementsFormService {

    private final MovementRepository movementRepository;
    private final MovementTypeRefRepository movementTypeRefRepository;
    private final PlaceTypeRefRepository placeTypeRefRepository;
    private final ValidationStatusRefRepository validationStatusRefRepository;
    private final VehicleTypeRefRepository vehicleTypeRefRepository;
    private final ObjectMapper objectMapper;

    public MovementsFormService(
            MovementRepository movementRepository,
            MovementTypeRefRepository movementTypeRefRepository,
            PlaceTypeRefRepository placeTypeRefRepository,
            ValidationStatusRefRepository validationStatusRefRepository,
            VehicleTypeRefRepository vehicleTypeRefRepository,
            ObjectMapper objectMapper
    ) {
        this.movementRepository = movementRepository;
        this.movementTypeRefRepository = movementTypeRefRepository;
        this.placeTypeRefRepository = placeTypeRefRepository;
        this.validationStatusRefRepository = validationStatusRefRepository;
        this.vehicleTypeRefRepository = vehicleTypeRefRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает данные формы и сохраняет перемещения в базу данных.
     *
     * @param formDto данные формы
     * @return список сохранённых перемещений
     */
    @Transactional
    public List<Movement> processForm(MovementsFormDto formDto) {
        LocalDate movementDate = parseDate(formDto.getMovementsDate());
        ValidationStatusRef pendingReviewStatus = validationStatusRefRepository
                .findByCode(ValidationStatus.PENDING_REVIEW)
                .orElseThrow(() -> new IllegalStateException("ValidationStatus PENDING_REVIEW not found"));

        List<Movement> savedMovements = new ArrayList<>();

        for (MovementItemDto movementItem : formDto.getMovements()) {
            Movement movement = convertToMovement(movementItem, movementDate, pendingReviewStatus);
            Movement saved = movementRepository.save(movement);
            if (saved != null) {
                savedMovements.add(saved);
            }
        }

        return savedMovements;
    }

    /**
     * Преобразует DTO перемещения в сущность Movement.
     *
     * @param movementItem DTO перемещения
     * @param day дата перемещения
     * @param validationStatus статус валидации
     * @return сущность Movement
     */
    private Movement convertToMovement(
            MovementItemDto movementItem,
            LocalDate day,
            ValidationStatusRef validationStatus
    ) {
        Movement movement = new Movement();

        // Тип перемещения
        MovementType movementType = MovementType.valueOf(movementItem.getMovementType());
        MovementTypeRef movementTypeRef = movementTypeRefRepository
                .findByCode(movementType)
                .orElseThrow(() -> new IllegalArgumentException("MovementType not found: " + movementItem.getMovementType()));
        movement.setType(movementTypeRef);

        // Дата
        movement.setDay(day);

        // Время отправления и прибытия
        movement.setDepartureTime(parseTime(movementItem.getDepartureTime(), day));
        movement.setDestinationTime(parseTime(movementItem.getArrivalTime(), day));

        // Типы мест отправления и прибытия
        PlaceType departurePlaceType = PlaceType.valueOf(movementItem.getDeparturePlace());
        PlaceTypeRef departurePlaceTypeRef = placeTypeRefRepository
                .findByCode(departurePlaceType)
                .orElseThrow(() -> new IllegalArgumentException("PlaceType not found: " + movementItem.getDeparturePlace()));
        movement.setDepartureType(departurePlaceTypeRef);

        PlaceType arrivalPlaceType = PlaceType.valueOf(movementItem.getArrivalPlace());
        PlaceTypeRef arrivalPlaceTypeRef = placeTypeRefRepository
                .findByCode(arrivalPlaceType)
                .orElseThrow(() -> new IllegalArgumentException("PlaceType not found: " + movementItem.getArrivalPlace()));
        movement.setDestinationType(arrivalPlaceTypeRef);

        // Адреса (преобразуются в JsonNode)
        if (movementItem.getDepartureAddress() != null) {
            movement.setDeparturePlace(convertAddressToJsonNode(movementItem.getDepartureAddress()));
        }
        if (movementItem.getArrivalAddress() != null) {
            movement.setDestinationPlace(convertAddressToJsonNode(movementItem.getArrivalAddress()));
        }

        // Статус валидации
        movement.setValidationStatus(validationStatus);

        // Тип транспортного средства (если указан)
        if (movementItem.getTransport() != null && !movementItem.getTransport().isEmpty()) {
            // Берём первый тип транспорта из списка
            String firstTransport = movementItem.getTransport().get(0);
            try {
                VehicleType vehicleType = VehicleType.valueOf(firstTransport);
                vehicleTypeRefRepository.findByCode(vehicleType)
                        .ifPresent(movement::setVehicleType);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверный тип транспорта
            }
        }

        // Стоимость (если указана)
        // В форме нет прямого поля стоимости, но можно вычислить из других данных
        // Пока оставляем null

        // Время ожидания
        if (movementItem.getWaitAtStartMinutes() != null) {
            movement.setWaitingTime(movementItem.getWaitAtStartMinutes());
        } else if (movementItem.getWaitBetweenTransfersMinutes() != null) {
            try {
                movement.setWaitingTime(Integer.parseInt(movementItem.getWaitBetweenTransfersMinutes()));
            } catch (NumberFormatException e) {
                // Игнорируем неверный формат
            }
        }

        // Количество мест
        if (movementItem.getNumberPeopleInCar() != null) {
            movement.setSeatsAmount(movementItem.getNumberPeopleInCar());
        }

        return movement;
    }

    /**
     * Преобразует адрес в GeoJSON Point для хранения в JSONB.
     * Извлекает координаты из данных адреса и создаёт GeoJSON Point используя mapbox-java.
     *
     * @param addressDto DTO адреса
     * @return JsonNode с GeoJSON Point или полными данными адреса, если координаты отсутствуют
     */
    private JsonNode convertAddressToJsonNode(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        // Пытаемся извлечь координаты из data.geo_lat и data.geo_lon
        JsonNode dataNode = addressDto.getData();
        if (dataNode != null && dataNode.has("geo_lat") && dataNode.has("geo_lon")) {
            try {
                String latStr = dataNode.get("geo_lat").asText();
                String lonStr = dataNode.get("geo_lon").asText();
                
                if (latStr != null && !latStr.isEmpty() && lonStr != null && !lonStr.isEmpty()) {
                    double latitude = Double.parseDouble(latStr);
                    double longitude = Double.parseDouble(lonStr);
                    
                    // Создаём GeoJSON Point используя mapbox-java (longitude, latitude)
                    Point geoJsonPoint = Point.fromLngLat(longitude, latitude);
                    
                    // Преобразуем Point в JsonNode через toJson() метод
                    String geoJsonString = geoJsonPoint.toJson();
                    try {
                        return objectMapper.readTree(geoJsonString);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        // Если не удалось распарсить JSON, возвращаем полные данные адреса
                    }
                }
            } catch (NumberFormatException | NullPointerException e) {
                // Если не удалось распарсить координаты, возвращаем полные данные адреса
            }
        }
        
        // Если координаты отсутствуют, возвращаем полные данные адреса
        return objectMapper.valueToTree(addressDto);
    }

    /**
     * Парсит строку даты в LocalDate.
     *
     * @param dateStr строка даты в формате "yyyy-MM-dd"
     * @return LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(dateStr);
    }

    /**
     * Парсит строку времени и комбинирует с датой для создания OffsetDateTime.
     *
     * @param timeStr строка времени в формате "HH:mm"
     * @param day дата
     * @return OffsetDateTime
     */
    private OffsetDateTime parseTime(String timeStr, LocalDate day) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        LocalTime time = LocalTime.parse(timeStr);
        return OffsetDateTime.of(day, time, ZoneOffset.UTC);
    }
}

