-- Create reference tables
CREATE TABLE IF NOT EXISTS ref_movement_type
(
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(64)  NOT NULL UNIQUE,
    description_ru VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS ref_place_type
(
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(128) NOT NULL UNIQUE,
    description_ru VARCHAR(512) NOT NULL
);

CREATE TABLE IF NOT EXISTS ref_vehicle_type
(
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(128) NOT NULL UNIQUE,
    description_ru VARCHAR(512) NOT NULL
);

CREATE TABLE IF NOT EXISTS ref_validation_status
(
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(128) NOT NULL UNIQUE,
    description_ru VARCHAR(512) NOT NULL
);

CREATE TABLE IF NOT EXISTS social_statuses
(
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(64)  NOT NULL UNIQUE,
    description_ru VARCHAR(256) NOT NULL
);

INSERT INTO social_statuses (id, code, description_ru)
VALUES (1, 'WORKING', 'работающий'),
       (2, 'STUDENT', 'школьник'),
       (3, 'UNIVERSITY_STUDENT', 'студент'),
       (4, 'PENSIONER', 'пенсионер по возрасту'),
       (5, 'PERSON_WITH_DISABILITIES', 'человек c ограниченными возможностями'),
       (6, 'UNEMPLOYED', 'безработный'),
       (7, 'HOUSEWIFE', 'домохозяйка'),
       (8, 'TEMPORARILY_UNEMPLOYED', 'временно нетрудящийся (декретный отпуск, отпуск по уходу за ребенком)')
ON CONFLICT (code) DO NOTHING;


INSERT INTO ref_validation_status (id, code, description_ru)
VALUES (1, 'PENDING_REVIEW', 'Ожидает проверки'),
       (2, 'VALID', 'Корректно'),
       (3, 'INVALID', 'Не корректно'),
       (4, 'PENDING_DELETION', 'Ожидает удаления')
ON CONFLICT (code) DO NOTHING;


-- Seed movement types
INSERT INTO ref_movement_type(id, code, description_ru)
VALUES (1, 'ON_FOOT', 'пешком'),
       (2, 'TRANSPORT', 'транспорт')
ON CONFLICT (code) DO NOTHING;

-- Seed place types
INSERT INTO ref_place_type(id, code, description_ru)
VALUES (1, 'HOME_RESIDENCE', 'дом - место жительства'),
       (2, 'FRIENDS_RELATIVES_HOME', 'дом друзей / родственников'),
       (3, 'WORKPLACE', 'работа / рабочее место'),
       (4, 'WORK_BUSINESS_TRIP', 'работа - служебная поездка'),
       (5, 'DAYCARE_CENTER', 'детский сад'),
       (6, 'SCHOOL', 'школа'),
       (7, 'COLLEGE_TECHNICAL_SCHOOL', 'колледж / техникум / училище'),
       (8, 'UNIVERSITY_INSTITUTE', 'университет / институт'),
       (9, 'HOSPITAL_CLINIC', 'больница / поликлиника'),
       (10, 'CULTURAL_INSTITUTION', 'учреждение культуры (музей, театр, цирк, библиотека и т.п.)'),
       (11, 'SPORT_FITNESS', 'спорт / фитнес'),
       (12, 'STORE_MARKET', 'магазин / рынок'),
       (13, 'SHOPPING_ENTERTAINMENT_CENTER', 'торгово - развлекательный центр'),
       (14, 'RESTAURANT_CAFE', 'ресторан / кафе / пункт общественного питания'),
       (15, 'SUBURB', 'пригород'),
       (16, 'OTHER', 'другое')
ON CONFLICT (code) DO NOTHING;

-- Seed vehicle types
INSERT INTO ref_vehicle_type(id, code, description_ru)
VALUES (1, 'BICYCLE', 'велосипед'),
       (2, 'INDIVIDUAL_MOBILITY', 'средства индивидуальной мобильности (самокат и пр.)'),
       (3, 'BUS', 'автобус'),
       (4, 'SHUTTLE_TAXI', 'маршрутное такси'),
       (5, 'TRAM', 'трамвай'),
       (6, 'PRIVATE_CAR', 'личный автомобиль'),
       (7, 'TROLLEYBUS', 'троллейбус'),
       (8, 'SUBURBAN_TRAIN', 'электричка'),
       (9, 'METRO', 'метро'),
       (10, 'TAXI', 'такси'),
       (11, 'CAR_SHARING', 'каршеринг'),
       (12, 'CITY_BIKE_RENTAL', 'городской велопрокат'),
       (13, 'SERVICE', 'служебный транспорт')
ON CONFLICT (code) DO NOTHING;

-- Create movements table (JSONB for places, FKs to reference tables)
CREATE TABLE IF NOT EXISTS movements
(
    id                           BIGSERIAL PRIMARY KEY,
    movement_type_id             BIGINT NOT NULL REFERENCES ref_movement_type (id),
    departure_time               TIMESTAMPTZ,
    destination_time             TIMESTAMPTZ,
    day                          DATE,
    departure_place              JSONB,
    destination_place            JSONB,
    departure_place_address      VARCHAR(512),
    destination_place_address    VARCHAR(512),
    departure_place_type_id      BIGINT NOT NULL REFERENCES ref_place_type (id),
    validation_status_id         BIGINT NOT NULL REFERENCES ref_validation_status (id),
    destination_place_type_id    BIGINT NOT NULL REFERENCES ref_place_type (id),
    vehicle_type_id              BIGINT NULL REFERENCES ref_vehicle_type (id),
    cost                         NUMERIC(12, 2),
    waiting_time                 INTEGER,
    seats_amount                 INTEGER
);

-- Seed sample movements (using enum ids now)
INSERT INTO movements(id,
                      movement_type_id,
                      departure_time,
                      destination_time,
                      day,
                      departure_place,
                      destination_place,
                      departure_place_address,
                      destination_place_address,
                      validation_status_id,
                      departure_place_type_id,
                      destination_place_type_id,
                      vehicle_type_id,
                      cost,
                      waiting_time,
                      seats_amount)
VALUES (1,
        2, -- TRANSPORT (id = 2 in ref_movement_type)
        '2025-10-25T08:15:00Z',
        '2025-10-25T08:55:00Z',
        '2025-10-25',
        '{
            "type": "Point",
            "coordinates": [
                37.6173,
                55.7558
            ]
        }'::jsonb,
        '{
            "type": "Point",
            "coordinates": [
                37.64,
                55.76
            ]
        }'::jsonb,
        'г. Москва, ул. Ленина, д. 10',            -- departure_place_address
        'г. Санкт-Петербург, Невский проспект, д. 50', -- destination_place_address
        1, -- PENDING_REVIEW (id = 1 in ref_validation_status)
        1, -- HOME_RESIDENCE (id = 1 in ref_place_type)
        3, -- WORKPLACE (id = 3 in ref_place_type)
        9, -- METRO (id = 9 in ref_vehicle_type)
        62.00,
        3,
        1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO movements(id,
                      movement_type_id,
                      departure_time,
                      destination_time,
                      day,
                      departure_place,
                      destination_place,
                      departure_place_address,
                      destination_place_address,
                      validation_status_id,
                      departure_place_type_id,
                      destination_place_type_id,
                      vehicle_type_id,
                      cost,
                      waiting_time,
                      seats_amount)
VALUES (2,
        1, -- ON_FOOT (id = 1 in ref_movement_type)
        '2025-10-26T09:00:00Z',
        '2025-10-26T09:20:00Z',
        '2025-10-26',
        '{
            "type": "Point",
            "coordinates": [
                37.60,
                55.75
            ]
        }'::jsonb,
        '{
            "type": "Point",
            "coordinates": [
                37.61,
                55.76
            ]
        }'::jsonb,
        'г. Москва, ул. Арбат, д. 15',         -- departure_place_address
        'г. Москва, ул. Тверская, д. 22',      -- destination_place_address
        1, -- PENDING_REVIEW (id = 1 in ref_validation_status)
        1, -- HOME_RESIDENCE (id = 1 in ref_place_type)
        12, -- STORE_MARKET (id = 12 in ref_place_type)
        NULL,
        0.00,
        0,
        0)
ON CONFLICT (id) DO NOTHING;


-- Table for roles
CREATE TABLE IF NOT EXISTS roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Table for scopes
CREATE TABLE IF NOT EXISTS scopes
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- TODO make username unique after legacy migration
-- Table for users
CREATE TABLE IF NOT EXISTS users
(
    id                      BIGSERIAL PRIMARY KEY,
    username                VARCHAR(255) NOT NULL UNIQUE,
    password                VARCHAR(255),
    enabled                 BOOLEAN DEFAULT true,
    locked                  BOOLEAN DEFAULT false,
    max_salary              INTEGER,
    min_salary              INTEGER,
    home_place              JSONB,
    home_readable_place     VARCHAR(255),
    transportation_cost_min INTEGER,
    transportation_cost_max INTEGER,
    birthday                DATE,
    gender                  VARCHAR(10),
    social_status_id        BIGINT REFERENCES social_statuses (id),
    last_login              DATE    DEFAULT CURRENT_DATE,
    creation_date           DATE    DEFAULT CURRENT_DATE
);

-- Association table: users_roles (Many-to-Many between users and roles)
CREATE TABLE IF NOT EXISTS users_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Association table: users_scopes (Many-to-Many between users and scopes)
CREATE TABLE IF NOT EXISTS users_scopes
(
    user_id  BIGINT NOT NULL,
    scope_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, scope_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (scope_id) REFERENCES scopes (id) ON DELETE CASCADE
);

-- Association table: role_scopes (Many-to-Many between roles and scope strings)
CREATE TABLE IF NOT EXISTS role_scopes
(
    role_id BIGINT       NOT NULL,
    scope   VARCHAR(255) NOT NULL,
    PRIMARY KEY (role_id, scope),
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);


-- Insert test roles (only columns: id, name)
INSERT INTO roles (id, name)
VALUES (101, 'ADMIN'),
       (102, 'MANAGER'),
       (103, 'USER'),
       (104, 'DEVELOPER')
ON CONFLICT (id) DO NOTHING;

-- Insert test scopes (only columns: id, name)
INSERT INTO scopes (id, name)
VALUES (201, 'READ'),
       (202, 'WRITE'),
       (203, 'UPDATE'),
       (204, 'DELETE')
ON CONFLICT (id) DO NOTHING;

-- Example assignment of scopes directly to roles via role_scopes table
INSERT INTO role_scopes (role_id, scope)
VALUES (101, 'READ'),
       (101, 'WRITE'),
       (101, 'UPDATE'),
       (101, 'DELETE'),
       (102, 'READ'),
       (102, 'WRITE'),
       (103, 'READ'),
       (104, 'READ')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS one_time_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    token_value VARCHAR(255) NOT NULL,
    username    VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP    NOT NULL
);

-- Spring Session JDBC tables
-- Схема таблиц для хранения сессий в базе данных

CREATE TABLE IF NOT EXISTS spring_session
(
    primary_id            CHAR(36) NOT NULL,
    session_id            VARCHAR(36) NOT NULL,
    creation_time         BIGINT NOT NULL,
    last_access_time      BIGINT NOT NULL,
    max_inactive_interval INTEGER NOT NULL,
    expiry_time           BIGINT NOT NULL,
    principal_name        VARCHAR(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS spring_session_ix1 ON spring_session (session_id);
CREATE INDEX IF NOT EXISTS spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX IF NOT EXISTS spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE IF NOT EXISTS spring_session_attributes
(
    session_primary_id CHAR(36) NOT NULL,
    attribute_name     VARCHAR(200) NOT NULL,
    attribute_bytes    BYTEA NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id) REFERENCES spring_session (primary_id) ON DELETE CASCADE
);
