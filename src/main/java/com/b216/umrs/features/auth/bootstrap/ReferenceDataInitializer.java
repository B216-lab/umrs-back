package com.b216.umrs.features.auth.bootstrap;

import com.b216.umrs.features.auth.domain.RoleRef;
import com.b216.umrs.features.auth.domain.ScopeRef;
import com.b216.umrs.features.auth.domain.SocialStatusRef;
import com.b216.umrs.features.auth.model.SocialStatus;
import com.b216.umrs.features.auth.model.Role;
import com.b216.umrs.features.auth.model.Scope;
import com.b216.umrs.features.auth.repository.RoleRepository;
import com.b216.umrs.features.auth.repository.ScopeRepository;
import com.b216.umrs.features.auth.repository.SocialStatusRepository;
import com.b216.umrs.features.movement.domain.MovementTypeRef;
import com.b216.umrs.features.movement.domain.PlaceTypeRef;
import com.b216.umrs.features.movement.domain.ValidationStatusRef;
import com.b216.umrs.features.movement.domain.VehicleTypeRef;
import com.b216.umrs.features.movement.model.MovementType;
import com.b216.umrs.features.movement.model.PlaceType;
import com.b216.umrs.features.movement.model.ValidationStatus;
import com.b216.umrs.features.movement.model.VehicleType;
import com.b216.umrs.features.movement.repository.MovementTypeRefRepository;
import com.b216.umrs.features.movement.repository.PlaceTypeRefRepository;
import com.b216.umrs.features.movement.repository.ValidationStatusRefRepository;
import com.b216.umrs.features.movement.repository.VehicleTypeRefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Инициализатор справочных данных.
 * Создаёт все необходимые справочники при первом запуске приложения.
 * Выполняется перед AdminUserInitializer, чтобы роли были доступны.
 */
@Component
@Order(1) // Выполняется перед AdminUserInitializer (который имеет @Order по умолчанию, т.е. Integer.MAX_VALUE)
public class ReferenceDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataInitializer.class);

    private final SocialStatusRepository socialStatusRepository;
    private final ValidationStatusRefRepository validationStatusRefRepository;
    private final MovementTypeRefRepository movementTypeRefRepository;
    private final PlaceTypeRefRepository placeTypeRefRepository;
    private final VehicleTypeRefRepository vehicleTypeRefRepository;
    private final RoleRepository roleRepository;
    private final ScopeRepository scopeRepository;

    public ReferenceDataInitializer(
        SocialStatusRepository socialStatusRepository,
        ValidationStatusRefRepository validationStatusRefRepository,
        MovementTypeRefRepository movementTypeRefRepository,
        PlaceTypeRefRepository placeTypeRefRepository,
        VehicleTypeRefRepository vehicleTypeRefRepository,
        RoleRepository roleRepository,
        ScopeRepository scopeRepository
    ) {
        this.socialStatusRepository = socialStatusRepository;
        this.validationStatusRefRepository = validationStatusRefRepository;
        this.movementTypeRefRepository = movementTypeRefRepository;
        this.placeTypeRefRepository = placeTypeRefRepository;
        this.vehicleTypeRefRepository = vehicleTypeRefRepository;
        this.roleRepository = roleRepository;
        this.scopeRepository = scopeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing reference data...");

        initializeSocialStatuses();
        initializeValidationStatuses();
        initializeMovementTypes();
        initializePlaceTypes();
        initializeVehicleTypes();
        initializeRoles();
        initializeScopes();
        initializeRoleScopes();

        log.info("Reference data initialization completed");
    }

    private void initializeSocialStatuses() {
        Map<SocialStatus, String> statuses = Map.of(
            SocialStatus.WORKING, "работающий",
            SocialStatus.STUDENT, "школьник",
            SocialStatus.UNIVERSITY_STUDENT, "студент",
            SocialStatus.PENSIONER, "пенсионер по возрасту",
            SocialStatus.PERSON_WITH_DISABILITIES, "человек c ограниченными возможностями",
            SocialStatus.UNEMPLOYED, "безработный",
            SocialStatus.HOUSEWIFE, "домохозяйка",
            SocialStatus.TEMPORARILY_UNEMPLOYED, "временно нетрудящийся (декретный отпуск, отпуск по уходу за ребенком)"
        );

        for (Map.Entry<SocialStatus, String> entry : statuses.entrySet()) {
            socialStatusRepository.findByCode(entry.getKey()).orElseGet(() -> {
                SocialStatusRef status = new SocialStatusRef();
                status.setCode(entry.getKey());
                status.setDescriptionRu(entry.getValue());
                return socialStatusRepository.save(status);
            });
        }
        log.debug("Social statuses initialized");
    }

    private void initializeValidationStatuses() {
        Map<ValidationStatus, String> statuses = Map.of(
            ValidationStatus.PENDING_REVIEW, "Ожидает проверки",
            ValidationStatus.VALID, "Корректно",
            ValidationStatus.INVALID, "Не корректно",
            ValidationStatus.PENDING_DELETION, "Ожидает удаления"
        );

        for (Map.Entry<ValidationStatus, String> entry : statuses.entrySet()) {
            validationStatusRefRepository.findByCode(entry.getKey()).orElseGet(() -> {
                ValidationStatusRef status = new ValidationStatusRef();
                status.setCode(entry.getKey());
                status.setDescriptionRu(entry.getValue());
                return validationStatusRefRepository.save(status);
            });
        }
        log.debug("Validation statuses initialized");
    }

    private void initializeMovementTypes() {
        Map<MovementType, String> types = Map.of(
            MovementType.ON_FOOT, "пешком",
            MovementType.TRANSPORT, "транспорт"
        );

        for (Map.Entry<MovementType, String> entry : types.entrySet()) {
            movementTypeRefRepository.findByCode(entry.getKey()).orElseGet(() -> {
                MovementTypeRef type = new MovementTypeRef();
                type.setCode(entry.getKey());
                type.setDescriptionRu(entry.getValue());
                return movementTypeRefRepository.save(type);
            });
        }
        log.debug("Movement types initialized");
    }

    private void initializePlaceTypes() {
        Map<PlaceType, String> types = Map.ofEntries(
            Map.entry(PlaceType.HOME_RESIDENCE, "дом - место жительства"),
            Map.entry(PlaceType.FRIENDS_RELATIVES_HOME, "дом друзей / родственников"),
            Map.entry(PlaceType.WORKPLACE, "работа / рабочее место"),
            Map.entry(PlaceType.WORK_BUSINESS_TRIP, "работа - служебная поездка"),
            Map.entry(PlaceType.DAYCARE_CENTER, "детский сад"),
            Map.entry(PlaceType.SCHOOL, "школа"),
            Map.entry(PlaceType.COLLEGE_TECHNICAL_SCHOOL, "колледж / техникум / училище"),
            Map.entry(PlaceType.UNIVERSITY_INSTITUTE, "университет / институт"),
            Map.entry(PlaceType.HOSPITAL_CLINIC, "больница / поликлиника"),
            Map.entry(PlaceType.CULTURAL_INSTITUTION, "учреждение культуры (музей, театр, цирк, библиотека и т.п.)"),
            Map.entry(PlaceType.SPORT_FITNESS, "спорт / фитнес"),
            Map.entry(PlaceType.STORE_MARKET, "магазин / рынок"),
            Map.entry(PlaceType.SHOPPING_ENTERTAINMENT_CENTER, "торгово - развлекательный центр"),
            Map.entry(PlaceType.RESTAURANT_CAFE, "ресторан / кафе / пункт общественного питания"),
            Map.entry(PlaceType.SUBURB, "пригород"),
            Map.entry(PlaceType.OTHER, "другое")
        );

        for (Map.Entry<PlaceType, String> entry : types.entrySet()) {
            placeTypeRefRepository.findByCode(entry.getKey()).orElseGet(() -> {
                PlaceTypeRef type = new PlaceTypeRef();
                type.setCode(entry.getKey());
                type.setDescriptionRu(entry.getValue());
                return placeTypeRefRepository.save(type);
            });
        }
        log.debug("Place types initialized");
    }

    private void initializeVehicleTypes() {
        Map<VehicleType, String> types = Map.ofEntries(
            Map.entry(VehicleType.BICYCLE, "велосипед"),
            Map.entry(VehicleType.INDIVIDUAL_MOBILITY, "средства индивидуальной мобильности (самокат и пр.)"),
            Map.entry(VehicleType.BUS, "автобус"),
            Map.entry(VehicleType.SHUTTLE_TAXI, "маршрутное такси"),
            Map.entry(VehicleType.TRAM, "трамвай"),
            Map.entry(VehicleType.PRIVATE_CAR, "личный автомобиль"),
            Map.entry(VehicleType.TROLLEYBUS, "троллейбус"),
            Map.entry(VehicleType.SUBURBAN_TRAIN, "электричка"),
            Map.entry(VehicleType.METRO, "метро"),
            Map.entry(VehicleType.TAXI, "такси"),
            Map.entry(VehicleType.CAR_SHARING, "каршеринг"),
            Map.entry(VehicleType.CITY_BIKE_RENTAL, "городской велопрокат"),
            Map.entry(VehicleType.SERVICE, "служебный транспорт")
        );

        for (Map.Entry<VehicleType, String> entry : types.entrySet()) {
            vehicleTypeRefRepository.findByCode(entry.getKey()).orElseGet(() -> {
                VehicleTypeRef type = new VehicleTypeRef();
                type.setCode(entry.getKey());
                type.setDescriptionRu(entry.getValue());
                return vehicleTypeRefRepository.save(type);
            });
        }
        log.debug("Vehicle types initialized");
    }

    private void initializeRoles() {
        for (Role roleEnum : Role.values()) {
            roleRepository.findByName(roleEnum).orElseGet(() -> {
                RoleRef role = new RoleRef();
                role.setName(roleEnum);
                return roleRepository.save(role);
            });
        }
        log.debug("Roles initialized");
    }

    private void initializeScopes() {
        for (Scope scopeEnum : Scope.values()) {
            scopeRepository.findByName(scopeEnum).orElseGet(() -> {
                ScopeRef scope = new ScopeRef();
                scope.setName(scopeEnum);
                return scopeRepository.save(scope);
            });
        }
        log.debug("Scopes initialized");
    }

    private void initializeRoleScopes() {
        // Получаем роли
        RoleRef adminRole = roleRepository.findByName(Role.ADMIN)
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
        RoleRef managerRole = roleRepository.findByName(Role.MANAGER)
            .orElseThrow(() -> new IllegalStateException("MANAGER role not found"));
        RoleRef userRole = roleRepository.findByName(Role.USER)
            .orElseThrow(() -> new IllegalStateException("USER role not found"));
        RoleRef developerRole = roleRepository.findByName(Role.DEVELOPER)
            .orElseThrow(() -> new IllegalStateException("DEVELOPER role not found"));

        // Назначаем scopes для ролей (только если ещё не назначены)
        // ADMIN: все права
        Set<String> adminScopes = adminRole.getScopes();
        if (adminScopes.isEmpty()) {
            adminScopes.add("READ");
            adminScopes.add("WRITE");
            adminScopes.add("UPDATE");
            adminScopes.add("DELETE");
            roleRepository.save(adminRole);
        }

        // MANAGER: READ, WRITE
        Set<String> managerScopes = managerRole.getScopes();
        if (managerScopes.isEmpty()) {
            managerScopes.add("READ");
            managerScopes.add("WRITE");
            roleRepository.save(managerRole);
        }

        // USER: только READ
        Set<String> userScopes = userRole.getScopes();
        if (userScopes.isEmpty()) {
            userScopes.add("READ");
            roleRepository.save(userRole);
        }

        // DEVELOPER: только READ
        Set<String> developerScopes = developerRole.getScopes();
        if (developerScopes.isEmpty()) {
            developerScopes.add("READ");
            roleRepository.save(developerRole);
        }

        log.debug("Role scopes initialized");
    }
}

