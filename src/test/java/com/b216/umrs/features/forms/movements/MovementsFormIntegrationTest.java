package com.b216.umrs.features.forms.movements;

import com.b216.umrs.features.auth.domain.User;
import com.b216.umrs.features.auth.util.TestUserFactory;
import com.b216.umrs.features.forms.movements.domain.MovementsFormRespondentKey;
import com.b216.umrs.features.forms.movements.dto.AddressDto;
import com.b216.umrs.features.forms.movements.dto.MovementItemDto;
import com.b216.umrs.features.forms.movements.dto.MovementsFormDto;
import com.b216.umrs.features.forms.movements.repository.MovementsFormRespondentKeyRepository;
import com.b216.umrs.features.forms.movements.repository.MovementsFormSubmissionRepository;
import com.b216.umrs.features.movement.repository.MovementRepository;
import com.b216.umrs.features.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tools.jackson.databind.ObjectMapper;

/**
 * Integration tests for movements form.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "UMRS_RUN_INTEGRATION_TESTS", matches = "true")
class MovementsFormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovementsFormRespondentKeyRepository respondentKeyRepository;

    @Autowired
    private MovementsFormSubmissionRepository submissionRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USER_EMAIL = "test_movements@test.local";
    private static final String TEST_USER_PASSWORD = "Test123!";
    private static final String OTHER_USER_EMAIL = "other_movements@test.local";
    private static final String OWNER_USER_EMAIL = "owner_movements@test.local";
    private static final String MOVEMENTS_FORM_ENDPOINT = "/api/v1/public/forms/movements";
    private static final String VALIDATE_RESPONDENT_KEY_ENDPOINT =
        "/api/v1/public/forms/movements/respondent-key/validate";
    private static final String ACTIVE_RESPONDENT_KEY = "respondent-key-active";
    private static final String INACTIVE_RESPONDENT_KEY = "respondent-key-inactive";

    @BeforeEach
    void setUp() {
        testUserFactory.ensureRegularUser(TEST_USER_EMAIL, TEST_USER_PASSWORD);
    }

    @AfterEach
    void tearDown() {
        movementRepository.deleteAll();
        submissionRepository.deleteAll();
        respondentKeyRepository.deleteAll();
        testUserFactory.deleteUser(TEST_USER_EMAIL);
        testUserFactory.deleteUser(OTHER_USER_EMAIL);
        testUserFactory.deleteUser(OWNER_USER_EMAIL);
    }

    @Test
    void should_validate_respondent_key_when_key_is_active() throws Exception {
        createRespondentKey(ACTIVE_RESPONDENT_KEY, null, true);

        mockMvc.perform(get(VALIDATE_RESPONDENT_KEY_ENDPOINT)
                .param("respondentKey", ACTIVE_RESPONDENT_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void should_return_not_found_when_respondent_key_is_unknown() throws Exception {
        mockMvc.perform(get(VALIDATE_RESPONDENT_KEY_ENDPOINT)
                .param("respondentKey", "missing-key"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Respondent key not found"));
    }

    @Test
    void should_return_not_found_when_respondent_key_is_inactive() throws Exception {
        createRespondentKey(INACTIVE_RESPONDENT_KEY, null, false);

        mockMvc.perform(get(VALIDATE_RESPONDENT_KEY_ENDPOINT)
                .param("respondentKey", INACTIVE_RESPONDENT_KEY))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Respondent key not found"));
    }

    /**
     * Test successful form submission with one on-foot movement.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void should_submit_form_when_respondent_key_is_valid() throws Exception {
        User user = userRepository.findByUsername(TEST_USER_EMAIL).orElseThrow();
        createRespondentKey(ACTIVE_RESPONDENT_KEY, user, true);
        MovementsFormDto formDto = createValidFormDto(ACTIVE_RESPONDENT_KEY);

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Данные формы успешно обработаны и сохранены"))
            .andExpect(jsonPath("$.savedMovementsCount").value(1));

        var savedSubmission = submissionRepository.findTopByOrderByIdDesc().orElseThrow();
        assertThat(savedSubmission.getRespondentKey().getKeyValue()).isEqualTo(ACTIVE_RESPONDENT_KEY);
        assertThat(savedSubmission.getUser()).isNotNull();
        assertThat(savedSubmission.getUser().getUsername()).isEqualTo(TEST_USER_EMAIL);
    }

    /**
     * Test form submission with multiple movements.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    void should_save_all_movements_when_form_contains_multiple_items() throws Exception {
        User user = userRepository.findByUsername(TEST_USER_EMAIL).orElseThrow();
        createRespondentKey(ACTIVE_RESPONDENT_KEY, user, true);
        MovementsFormDto formDto = createValidFormDto(ACTIVE_RESPONDENT_KEY);

        MovementItemDto secondMovement = createValidMovementItem();
        secondMovement.setDepartureTime("14:00");
        secondMovement.setArrivalTime("15:00");
        formDto.getMovements().add(secondMovement);

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
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
    void should_submit_form_when_movements_list_is_empty() throws Exception {
        User user = userRepository.findByUsername(TEST_USER_EMAIL).orElseThrow();
        createRespondentKey(ACTIVE_RESPONDENT_KEY, user, true);
        MovementsFormDto formDto = createValidFormDto(ACTIVE_RESPONDENT_KEY);
        formDto.setMovements(new ArrayList<>());

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
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
    void should_submit_transport_movement_when_payload_is_valid() throws Exception {
        User user = userRepository.findByUsername(TEST_USER_EMAIL).orElseThrow();
        createRespondentKey(ACTIVE_RESPONDENT_KEY, user, true);
        MovementsFormDto formDto = createValidFormDto(ACTIVE_RESPONDENT_KEY);
        MovementItemDto transportMovement = createValidMovementItem();
        transportMovement.setMovementType("TRANSPORT");
        transportMovement.setTransport(List.of("METRO"));
        formDto.setMovements(List.of(transportMovement));

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
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
    void should_submit_form_with_addresses_when_payload_is_valid() throws Exception {
        User user = userRepository.findByUsername(TEST_USER_EMAIL).orElseThrow();
        createRespondentKey(ACTIVE_RESPONDENT_KEY, user, true);
        MovementsFormDto formDto = createValidFormDto(ACTIVE_RESPONDENT_KEY);
        MovementItemDto movement = formDto.getMovements().get(0);

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
    void should_update_matching_user_profile_when_respondent_key_owner_matches_session() throws Exception {
        User user = userRepository.findByUsername(TEST_USER_EMAIL).orElseThrow();
        createRespondentKey(ACTIVE_RESPONDENT_KEY, user, true);
        MovementsFormDto formDto = createValidFormDto(ACTIVE_RESPONDENT_KEY);
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedMovementsCount").value(1));

        User updatedUser = userRepository.findByUsername(TEST_USER_EMAIL).orElseThrow();
        assertThat(updatedUser.getBirthday()).isNotNull();
        assertThat(updatedUser.getHomeReadablePlace()).isEqualTo("г. Москва, ул. Арбат, д. 15");
    }

    @Test
    void should_reject_submission_when_respondent_key_is_missing() throws Exception {
        MovementsFormDto formDto = createValidFormDto(null);
        formDto.setRespondentKey(null);

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void should_reject_submission_when_respondent_key_is_invalid() throws Exception {
        MovementsFormDto formDto = createValidFormDto("missing-key");

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Respondent key not found"));

        assertThat(submissionRepository.count()).isZero();
        assertThat(movementRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = OTHER_USER_EMAIL)
    void should_use_respondent_key_owner_when_session_user_differs() throws Exception {
        User ownerUser = testUserFactory.ensureRegularUser(OWNER_USER_EMAIL, TEST_USER_PASSWORD);
        testUserFactory.ensureRegularUser(OTHER_USER_EMAIL, TEST_USER_PASSWORD);
        createRespondentKey(ACTIVE_RESPONDENT_KEY, ownerUser, true);

        MovementsFormDto formDto = createValidFormDto(ACTIVE_RESPONDENT_KEY);
        formDto.setBirthday("1995-05-05");

        mockMvc.perform(post(MOVEMENTS_FORM_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedMovementsCount").value(1));

        var savedSubmission = submissionRepository.findTopByOrderByIdDesc().orElseThrow();
        assertThat(savedSubmission.getUser()).isNotNull();
        assertThat(savedSubmission.getUser().getUsername()).isEqualTo(OWNER_USER_EMAIL);

        User sessionUser = userRepository.findByUsername(OTHER_USER_EMAIL).orElseThrow();
        assertThat(sessionUser.getBirthday()).isNull();
    }

    /**
     * Creates valid form DTO for testing.
     */
    private MovementsFormDto createValidFormDto(String respondentKey) {
        MovementsFormDto formDto = new MovementsFormDto();
        formDto.setRespondentKey(respondentKey);
        formDto.setMovementsDate("2025-12-10");

        MovementItemDto movement = createValidMovementItem();
        List<MovementItemDto> movements = new ArrayList<>();
        movements.add(movement);
        formDto.setMovements(movements);

        return formDto;
    }

    private void createRespondentKey(String keyValue, User user, boolean active) {
        MovementsFormRespondentKey respondentKey = new MovementsFormRespondentKey();
        respondentKey.setKeyValue(keyValue);
        respondentKey.setUser(user);
        respondentKey.setActive(active);
        respondentKeyRepository.save(respondentKey);
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

