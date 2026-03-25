package com.b216.umrs.features.forms.movements.service;

import com.b216.umrs.features.auth.repository.SocialStatusRepository;
import com.b216.umrs.features.auth.repository.UserRepository;
import com.b216.umrs.features.forms.movements.dto.AddressDto;
import com.b216.umrs.features.forms.movements.dto.MovementItemDto;
import com.b216.umrs.features.forms.movements.dto.MovementsFormDto;
import com.b216.umrs.features.forms.movements.repository.MovementsFormSubmissionRepository;
import com.b216.umrs.features.movement.domain.Movement;
import com.b216.umrs.features.movement.domain.MovementTypeRef;
import com.b216.umrs.features.movement.domain.PlaceTypeRef;
import com.b216.umrs.features.movement.domain.ValidationStatusRef;
import com.b216.umrs.features.movement.model.MovementType;
import com.b216.umrs.features.movement.model.PlaceType;
import com.b216.umrs.features.movement.model.ValidationStatus;
import com.b216.umrs.features.movement.repository.MovementRepository;
import com.b216.umrs.features.movement.repository.MovementTypeRefRepository;
import com.b216.umrs.features.movement.repository.PlaceTypeRefRepository;
import com.b216.umrs.features.movement.repository.ValidationStatusRefRepository;
import com.b216.umrs.features.movement.repository.VehicleTypeRefRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementsFormServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private MovementTypeRefRepository movementTypeRefRepository;

    @Mock
    private PlaceTypeRefRepository placeTypeRefRepository;

    @Mock
    private ValidationStatusRefRepository validationStatusRefRepository;

    @Mock
    private VehicleTypeRefRepository vehicleTypeRefRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialStatusRepository socialStatusRepository;

    @Mock
    private MovementsFormSubmissionRepository movementsFormSubmissionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MovementsFormService movementsFormService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_save_departure_geojson_when_address_contains_geojson() {
        // given
        ValidationStatusRef pendingReview = new ValidationStatusRef();
        pendingReview.setCode(ValidationStatus.PENDING_REVIEW);
        when(validationStatusRefRepository.findByCode(ValidationStatus.PENDING_REVIEW)).thenReturn(Optional.of(pendingReview));

        MovementTypeRef movementTypeRef = new MovementTypeRef();
        movementTypeRef.setCode(MovementType.ON_FOOT);
        when(movementTypeRefRepository.findByCode(MovementType.ON_FOOT)).thenReturn(Optional.of(movementTypeRef));

        PlaceTypeRef homeResidence = new PlaceTypeRef();
        homeResidence.setCode(PlaceType.HOME_RESIDENCE);
        when(placeTypeRefRepository.findByCode(PlaceType.HOME_RESIDENCE)).thenReturn(Optional.of(homeResidence));

        PlaceTypeRef school = new PlaceTypeRef();
        school.setCode(PlaceType.SCHOOL);
        when(placeTypeRefRepository.findByCode(PlaceType.SCHOOL)).thenReturn(Optional.of(school));

        when(movementsFormSubmissionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
        when(movementRepository.save(movementCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ObjectNode geoJson = objectMapper.createObjectNode();
        geoJson.put("type", "Point");
        geoJson.putArray("coordinates").add(37.6173).add(55.7558);

        AddressDto departureAddress = new AddressDto();
        departureAddress.setValue("г. Москва, ул. Ленина, д. 10");
        departureAddress.setGeoJson(geoJson);

        MovementItemDto movementItem = new MovementItemDto();
        movementItem.setMovementType("ON_FOOT");
        movementItem.setDepartureTime("12:00");
        movementItem.setArrivalTime("13:00");
        movementItem.setDeparturePlace("HOME_RESIDENCE");
        movementItem.setArrivalPlace("SCHOOL");
        movementItem.setDepartureAddress(departureAddress);

        MovementsFormDto formDto = new MovementsFormDto();
        formDto.setMovementsDate("2025-12-10");
        formDto.setMovements(List.of(movementItem));

        // when
        movementsFormService.processForm(formDto);

        // then
        verify(movementRepository).save(any(Movement.class));
        Movement savedMovement = movementCaptor.getValue();

        assertThat(savedMovement.getDeparturePlace()).isNotNull();
        assertThat(savedMovement.getDeparturePlaceAddress()).isEqualTo("г. Москва, ул. Ленина, д. 10");
        assertThat(savedMovement.getDeparturePlace().get("type").asText()).isEqualTo("Point");
        assertThat(savedMovement.getDeparturePlace().get("coordinates").get(0).asDouble()).isEqualTo(37.6173);
        assertThat(savedMovement.getDeparturePlace().get("coordinates").get(1).asDouble()).isEqualTo(55.7558);
    }
}
