package com.b216.umrs.features.forms.movements;

import com.b216.umrs.features.auth.util.TestUserFactory;
import com.b216.umrs.features.forms.movements.dto.AddressDto;
import com.b216.umrs.features.forms.movements.dto.MovementItemDto;
import com.b216.umrs.features.forms.movements.dto.MovementsFormDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for movements form.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MovementsFormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USER_EMAIL = "test_movements@test.local";
    private static final String TEST_USER_PASSWORD = "Test123!";
    private static final String MOVEMENTS_FORM_ENDPOINT = "/api/v1/forms/movements";

    @BeforeEach
    void setUp() {
        // Create test user before each test
        testUserFactory.ensureRegularUser(TEST_USER_EMAIL, TEST_USER_PASSWORD);
    }

    @AfterEach
    void tearDown() {
        // Delete test user after each test
        testUserFactory.deleteUser(TEST_USER_EMAIL);
    }

    /**
     * Test successful form submission with one on-foot movement.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void givenValidFormData_whenSubmitForm_thenSuccess() throws Exception {
        MovementsFormDto formDto = createValidFormDto();

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Данные формы успешно обработаны и сохранены"))
            .andExpect(jsonPath("$.savedMovementsCount").value(1));
    }

    /**
     * Test form submission with multiple movements.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void givenFormWithMultipleMovements_whenSubmitForm_thenAllSaved() throws Exception {
        MovementsFormDto formDto = createValidFormDto();

        // Add second movement
        MovementItemDto secondMovement = createValidMovementItem();
        secondMovement.setDepartureTime("14:00");
        secondMovement.setArrivalTime("15:00");
        formDto.getMovements().add(secondMovement);

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedMovementsCount").value(2));
    }

    /**
     * Test form submission with empty movements list.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void givenFormWithEmptyMovements_whenSubmitForm_thenSuccess() throws Exception {
        MovementsFormDto formDto = createValidFormDto();
        formDto.setMovements(new ArrayList<>());

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedMovementsCount").value(0));
    }

    /**
     * Test form submission with TRANSPORT movement type.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void givenFormWithTransportMovement_whenSubmitForm_thenSuccess() throws Exception {
        MovementsFormDto formDto = createValidFormDto();
        MovementItemDto transportMovement = createValidMovementItem();
        transportMovement.setMovementType("TRANSPORT");
        transportMovement.setTransport(List.of("METRO"));
        formDto.setMovements(List.of(transportMovement));

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedMovementsCount").value(1));
    }

    /**
     * Test form submission with addresses (coordinates).
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void givenFormWithAddresses_whenSubmitForm_thenSuccess() throws Exception {
        MovementsFormDto formDto = createValidFormDto();
        MovementItemDto movement = formDto.getMovements().get(0);

        // Add addresses with coordinates
        AddressDto departureAddress = new AddressDto();
        departureAddress.setValue("г. Москва, ул. Ленина, д. 10");
        departureAddress.setLatitude(55.7558);
        departureAddress.setLongitude(37.6173);
        movement.setDepartureAddress(departureAddress);

        AddressDto arrivalAddress = new AddressDto();
        arrivalAddress.setValue("г. Москва, ул. Тверская, д. 22");
        arrivalAddress.setLatitude(55.7568);
        arrivalAddress.setLongitude(37.6056);
        movement.setArrivalAddress(arrivalAddress);

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedMovementsCount").value(1));
    }

    /**
     * Test form submission with user profile data.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void givenFormWithUserProfileData_whenSubmitForm_thenSuccess() throws Exception {
        MovementsFormDto formDto = createValidFormDto();
        formDto.setBirthday("1990-01-01");
        formDto.setGender("MALE");
        formDto.setSocialStatus("WORKING");
        formDto.setTransportCostMin(100);
        formDto.setTransportCostMax(5000);
        formDto.setIncomeMin(30000);
        formDto.setIncomeMax(100000);

        AddressDto homeAddress = new AddressDto();
        homeAddress.setValue("г. Москва, ул. Арбат, д. 15");
        homeAddress.setLatitude(55.7520);
        homeAddress.setLongitude(37.5920);
        formDto.setHomeAddress(homeAddress);

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedMovementsCount").value(1));
    }

    /**
     * Creates valid form DTO for testing.
     */
    private MovementsFormDto createValidFormDto() {
        MovementsFormDto formDto = new MovementsFormDto();
        formDto.setMovementsDate("2025-12-10");

        MovementItemDto movement = createValidMovementItem();
        List<MovementItemDto> movements = new ArrayList<>();
        movements.add(movement);
        formDto.setMovements(movements);

        return formDto;
    }

    /**
     * Creates valid movement DTO for testing.
     */
    private MovementItemDto createValidMovementItem() {
        MovementItemDto movement = new MovementItemDto();
        movement.setMovementType("ON_FOOT");
        movement.setDepartureTime("12:00");
        movement.setArrivalTime("13:00");
        movement.setDeparturePlace("HOME_RESIDENCE");
        movement.setArrivalPlace("SCHOOL");
        movement.setTransport(new ArrayList<>());
        movement.setNumberPeopleInCar(1);
        movement.setNumberOfTransfers(0);
        movement.setWaitBetweenTransfersMinutes("0");
        return movement;
    }
}

