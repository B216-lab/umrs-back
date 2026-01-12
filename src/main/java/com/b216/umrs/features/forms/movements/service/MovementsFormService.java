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
import com.b216.umrs.features.auth.model.Gender;
import com.b216.umrs.features.auth.repository.UserRepository;
import com.b216.umrs.features.auth.repository.SocialStatusRepository;
import com.b216.umrs.features.forms.movements.domain.MovementsFormSubmission;
import com.b216.umrs.features.forms.movements.repository.MovementsFormSubmissionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Point;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;
    private final SocialStatusRepository socialStatusRepository;
    private final MovementsFormSubmissionRepository movementsFormSubmissionRepository;
    private final ObjectMapper objectMapper;

    public MovementsFormService(
            MovementRepository movementRepository,
            MovementTypeRefRepository movementTypeRefRepository,
            PlaceTypeRefRepository placeTypeRefRepository,
            ValidationStatusRefRepository validationStatusRefRepository,
            VehicleTypeRefRepository vehicleTypeRefRepository,
            UserRepository userRepository,
            SocialStatusRepository socialStatusRepository,
            MovementsFormSubmissionRepository movementsFormSubmissionRepository,
            ObjectMapper objectMapper
    ) {
        this.movementRepository = movementRepository;
        this.movementTypeRefRepository = movementTypeRefRepository;
        this.placeTypeRefRepository = placeTypeRefRepository;
        this.validationStatusRefRepository = validationStatusRefRepository;
        this.vehicleTypeRefRepository = vehicleTypeRefRepository;
        this.userRepository = userRepository;
        this.socialStatusRepository = socialStatusRepository;
        this.movementsFormSubmissionRepository = movementsFormSubmissionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает данные формы и сохраняет перемещения в базу данных.
     * Создаёт запись MovementsFormSubmission для хранения данных пользователя из формы.
     * Также обновляет профиль пользователя, если данные предоставлены и пользователь аутентифицирован.
     *
     * @param formDto данные формы
     * @return список сохранённых перемещений
     */
    @Transactional
    public List<Movement> processForm(MovementsFormDto formDto) {
        // Создаём запись MovementsFormSubmission
        MovementsFormSubmission formSubmission = createMovementsFormSubmission(formDto);
        formSubmission = movementsFormSubmissionRepository.save(formSubmission);

        // Обновляем профиль пользователя, если пользователь аутентифицирован
        updateUserProfile(formDto);

        // Обрабатываем перемещения
        LocalDate movementDate = parseDate(formDto.getMovementsDate());
        ValidationStatusRef pendingReviewStatus = validationStatusRefRepository
                .findByCode(ValidationStatus.PENDING_REVIEW)
                .orElseThrow(() -> new IllegalStateException("ValidationStatus PENDING_REVIEW not found"));

        List<Movement> savedMovements = new ArrayList<>();

        for (MovementItemDto movementItem : formDto.getMovements()) {
            Movement movement = convertToMovement(movementItem, movementDate, pendingReviewStatus);
            movement.setMovementsFormSubmission(formSubmission);
            Movement saved = movementRepository.save(movement);
            if (saved != null) {
                savedMovements.add(saved);
            }
        }

        return savedMovements;
    }

    /**
     * Обновляет профиль текущего аутентифицированного пользователя данными из формы.
     *
     * @param formDto данные формы
     */
    private void updateUserProfile(MovementsFormDto formDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymous".equals(authentication.getPrincipal())) {
            return; // Пользователь не аутентифицирован
        }

        String username = authentication.getName();
        userRepository.findByUsername(username).ifPresent(user -> {
            boolean updated = false;

            // Обновляем день рождения
            if (formDto.getBirthday() != null && !formDto.getBirthday().isEmpty()) {
                try {
                    user.setBirthday(LocalDate.parse(formDto.getBirthday()));
                    updated = true;
                } catch (Exception e) {
                    // Игнорируем неверный формат даты
                }
            }

            // Обновляем пол
            if (formDto.getGender() != null && !formDto.getGender().isEmpty()) {
                try {
                    user.setGender(Gender.valueOf(formDto.getGender()));
                    updated = true;
                } catch (IllegalArgumentException e) {
                    // Игнорируем неверное значение пола
                }
            }

            // Обновляем социальный статус
            if (formDto.getSocialStatus() != null && !formDto.getSocialStatus().isEmpty()) {
                try {
                    com.b216.umrs.features.auth.model.SocialStatus statusEnum =
                        com.b216.umrs.features.auth.model.SocialStatus.valueOf(formDto.getSocialStatus());
                    socialStatusRepository.findByCode(statusEnum)
                        .ifPresent(user::setSocialStatus);
                    updated = true;
                } catch (IllegalArgumentException e) {
                    // Игнорируем неверное значение социального статуса
                }
            }

            // Обновляем расходы на транспорт
            if (formDto.getTransportCostMin() != null) {
                user.setTransportationCostMin(formDto.getTransportCostMin());
                updated = true;
            }
            if (formDto.getTransportCostMax() != null) {
                user.setTransportationCostMax(formDto.getTransportCostMax());
                updated = true;
            }

            // Обновляем доход
            if (formDto.getIncomeMin() != null) {
                user.setMinSalary(formDto.getIncomeMin());
                updated = true;
            }
            if (formDto.getIncomeMax() != null) {
                user.setMaxSalary(formDto.getIncomeMax());
                updated = true;
            }

            // Обновляем домашний адрес
            if (formDto.getHomeAddress() != null) {
                AddressDto homeAddress = formDto.getHomeAddress();
                if (homeAddress.getValue() != null) {
                    user.setHomeReadablePlace(homeAddress.getValue());
                }
                JsonNode homePlaceJson = convertAddressToJsonNode(homeAddress);
                if (homePlaceJson != null) {
                    user.setHomePlace(homePlaceJson);
                }
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
            }
        });
    }

    /**
     * Создаёт сущность MovementsFormSubmission из данных формы.
     * Связывает с пользователем, если он аутентифицирован.
     *
     * @param formDto данные формы
     * @return сущность MovementsFormSubmission
     */
    private MovementsFormSubmission createMovementsFormSubmission(MovementsFormDto formDto) {
        MovementsFormSubmission submission = new MovementsFormSubmission();

        // Связываем с пользователем, если он аутентифицирован
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymous".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            userRepository.findByUsername(username).ifPresent(submission::setUser);
        }

        // День рождения
        if (formDto.getBirthday() != null && !formDto.getBirthday().isEmpty()) {
            try {
                submission.setBirthday(LocalDate.parse(formDto.getBirthday()));
            } catch (Exception e) {
                // Игнорируем неверный формат даты
            }
        }

        // Пол
        if (formDto.getGender() != null && !formDto.getGender().isEmpty()) {
            try {
                submission.setGender(Gender.valueOf(formDto.getGender()));
            } catch (IllegalArgumentException e) {
                // Игнорируем неверное значение пола
            }
        }

        // Социальный статус
        if (formDto.getSocialStatus() != null && !formDto.getSocialStatus().isEmpty()) {
            try {
                com.b216.umrs.features.auth.model.SocialStatus statusEnum =
                    com.b216.umrs.features.auth.model.SocialStatus.valueOf(formDto.getSocialStatus());
                socialStatusRepository.findByCode(statusEnum)
                    .ifPresent(submission::setSocialStatus);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверное значение социального статуса
            }
        }

        // Расходы на транспорт
        submission.setTransportCostMin(formDto.getTransportCostMin());
        submission.setTransportCostMax(formDto.getTransportCostMax());

        // Доход
        submission.setIncomeMin(formDto.getIncomeMin());
        submission.setIncomeMax(formDto.getIncomeMax());

        // Домашний адрес
        if (formDto.getHomeAddress() != null) {
            AddressDto homeAddress = formDto.getHomeAddress();
            if (homeAddress.getValue() != null) {
                submission.setHomeReadableAddress(homeAddress.getValue());
            }
            JsonNode homePlaceJson = convertAddressToJsonNode(homeAddress);
            if (homePlaceJson != null) {
                submission.setHomeAddress(homePlaceJson);
            }
        }

        // Дата передвижений
        if (formDto.getMovementsDate() != null && !formDto.getMovementsDate().isEmpty()) {
            try {
                submission.setMovementsDate(LocalDate.parse(formDto.getMovementsDate()));
            } catch (Exception e) {
                // Игнорируем неверный формат даты
            }
        }

        return submission;
    }

    /**
     * Преобразует DTO перемещения в сущность Movement.
     *
     * @param movementItem DTO перемещения
     * @param movementDate дата перемещения (используется для парсинга времени)
     * @param validationStatus статус валидации
     * @return сущность Movement
     */
    private Movement convertToMovement(
            MovementItemDto movementItem,
            LocalDate movementDate,
            ValidationStatusRef validationStatus
    ) {
        Movement movement = new Movement();

        // Тип перемещения
        MovementType movementType = MovementType.valueOf(movementItem.getMovementType());
        MovementTypeRef movementTypeRef = movementTypeRefRepository
                .findByCode(movementType)
                .orElseThrow(() -> new IllegalArgumentException("MovementType not found: " + movementItem.getMovementType()));
        movement.setType(movementTypeRef);

        // Время отправления и прибытия
        movement.setDepartureTime(parseTime(movementItem.getDepartureTime(), movementDate));
        movement.setDestinationTime(parseTime(movementItem.getArrivalTime(), movementDate));

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

        // Адреса (преобразуются в JsonNode и сохраняются читаемые адреса)
        if (movementItem.getDepartureAddress() != null) {
            AddressDto departureAddress = movementItem.getDepartureAddress();
            movement.setDeparturePlace(convertAddressToJsonNode(departureAddress));
            // Сохраняем читаемый адрес
            if (departureAddress.getValue() != null) {
                movement.setDeparturePlaceAddress(departureAddress.getValue());
            }
        }
        if (movementItem.getArrivalAddress() != null) {
            AddressDto arrivalAddress = movementItem.getArrivalAddress();
            movement.setDestinationPlace(convertAddressToJsonNode(arrivalAddress));
            // Сохраняем читаемый адрес
            if (arrivalAddress.getValue() != null) {
                movement.setDestinationPlaceAddress(arrivalAddress.getValue());
            }
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

        // Комментарий
        if (movementItem.getComment() != null && !movementItem.getComment().isEmpty()) {
            movement.setComment(movementItem.getComment());
        }

        return movement;
    }

    /**
     * Преобразует адрес в GeoJSON Point для хранения в JSONB.
     * Использует координаты из DTO для создания GeoJSON Point.
     *
     * @param addressDto DTO адреса с координатами
     * @return JsonNode с GeoJSON Point или null, если координаты отсутствуют
     */
    private JsonNode convertAddressToJsonNode(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        // Извлекаем координаты напрямую из DTO
        Double latitude = addressDto.getLatitude();
        Double longitude = addressDto.getLongitude();
        
        if (latitude != null && longitude != null) {
            try {
                // Создаём GeoJSON Point используя mapbox-java (longitude, latitude)
                Point geoJsonPoint = Point.fromLngLat(longitude, latitude);
                
                // Преобразуем Point в JsonNode через toJson() метод
                String geoJsonString = geoJsonPoint.toJson();
                return objectMapper.readTree(geoJsonString);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                // Если не удалось создать GeoJSON, возвращаем null
                return null;
            } catch (Exception e) {
                // Обрабатываем другие ошибки (например, неверные координаты)
                return null;
            }
        }
        
        // Если координаты отсутствуют, возвращаем null
        return null;
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

