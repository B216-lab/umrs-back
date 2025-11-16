-- Create reference tables
CREATE TABLE IF NOT EXISTS ref_movement_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    description_ru VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS ref_place_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    description_ru VARCHAR(512) NOT NULL
);

CREATE TABLE IF NOT EXISTS ref_vehicle_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    description_ru VARCHAR(512) NOT NULL
);

CREATE TABLE IF NOT EXISTS ref_validation_status (
    id SERIAL PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    description_ru VARCHAR(512) NOT NULL
);

CREATE TABLE IF NOT EXISTS social_statuses (
    id SERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    description_ru VARCHAR(256) NOT NULL
);

INSERT INTO social_statuses (id, code, description_ru) VALUES
    (1, 'WORKING', 'работающий'),
    (2, 'STUDENT', 'школьник'),
    (3, 'UNIVERSITY_STUDENT', 'студент'),
    (4, 'PENSIONER', 'пенсионер по возрасту'),
    (5, 'PERSON_WITH_DISABILITIES', 'человек c ограниченными возможностями'),
    (6, 'UNEMPLOYED', 'безработный'),
    (7, 'HOUSEWIFE', 'домохозяйка'),
    (8, 'TEMPORARILY_UNEMPLOYED', 'временно нетрудящийся (декретный отпуск, отпуск по уходу за ребенком)')
ON CONFLICT (code) DO NOTHING;


INSERT INTO ref_validation_status (id, code, description_ru) VALUES
    (1, 'PENDING_REVIEW', 'Ожидает проверки'),
    (2, 'VALID', 'Корректно'),
    (3, 'INVALID', 'Не корректно'),
    (4, 'PENDING_DELETION', 'Ожидает удаления')
ON CONFLICT (code) DO NOTHING;


-- Seed movement types
INSERT INTO ref_movement_type(id, code, description_ru) VALUES
    (1, 'ON_FOOT', 'пешком'),
    (2, 'TRANSPORT', 'транспорт')
ON CONFLICT (code) DO NOTHING;

-- Seed place types
INSERT INTO ref_place_type(id, code, description_ru) VALUES
    (1, 'HOME_RESIDENCE','дом - место жительства'),
    (2, 'FRIENDS_RELATIVES_HOME','дом друзей / родственников'),
    (3, 'WORKPLACE','работа / рабочее место'),
    (4, 'WORK_BUSINESS_TRIP','работа - служебная поездка'),
    (5, 'DAYCARE_CENTER','детский сад'),
    (6, 'SCHOOL','школа'),
    (7, 'COLLEGE_TECHNICAL_SCHOOL','колледж / техникум / училище'),
    (8, 'UNIVERSITY_INSTITUTE','университет / институт'),
    (9, 'HOSPITAL_CLINIC','больница / поликлиника'),
    (10, 'CULTURAL_INSTITUTION','учреждение культуры (музей, театр, цирк, библиотека и т.п.)'),
    (11, 'SPORT_FITNESS','спорт / фитнес'),
    (12, 'STORE_MARKET','магазин / рынок'),
    (13, 'SHOPPING_ENTERTAINMENT_CENTER','торгово - развлекательный центр'),
    (14, 'RESTAURANT_CAFE','ресторан / кафе / пункт общественного питания'),
    (15, 'SUBURB','пригород'),
    (16, 'OTHER','другое')
ON CONFLICT (code) DO NOTHING;

-- Seed vehicle types
INSERT INTO ref_vehicle_type(id, code, description_ru) VALUES
    (1, 'BICYCLE','велосипед'),
    (2, 'INDIVIDUAL_MOBILITY','средства индивидуальной мобильности (самокат и пр.)'),
    (3, 'BUS','автобус'),
    (4, 'SHUTTLE_TAXI','маршрутное такси'),
    (5, 'TRAM','трамвай'),
    (6, 'PRIVATE_CAR','личный автомобиль'),
    (7, 'TROLLEYBUS','троллейбус'),
    (8, 'SUBURBAN_TRAIN','электричка'),
    (9, 'METRO','метро'),
    (10, 'TAXI','такси'),
    (11, 'CAR_SHARING','каршеринг'),
    (12, 'CITY_BIKE_RENTAL','городской велопрокат'),
    (13, 'SERVICE','служебный транспорт')
ON CONFLICT (code) DO NOTHING;

-- Create movements table (JSONB for places, FKs to reference tables)
CREATE TABLE IF NOT EXISTS movements (
    uuid UUID PRIMARY KEY,
    movement_type_id INTEGER NOT NULL REFERENCES ref_movement_type(id),
    departure_time TIMESTAMPTZ,
    destination_time TIMESTAMPTZ,
    day DATE,
    departure_place JSONB,
    destination_place JSONB,
    departure_place_type_id INTEGER NOT NULL REFERENCES ref_place_type(id),
    destination_place_type_id INTEGER NOT NULL REFERENCES ref_place_type(id),
    vehicle_type_id INTEGER NULL REFERENCES ref_vehicle_type(id),
    cost NUMERIC(12,2),
    waiting_time INTEGER,
    seats_amount INTEGER
);

-- Seed sample movements (using enum ids now)
INSERT INTO movements(
    uuid,
    movement_type_id,
    departure_time,
    destination_time,
    day,
    departure_place,
    destination_place,
    departure_place_type_id,
    destination_place_type_id,
    vehicle_type_id,
    cost,
    waiting_time,
    seats_amount
) VALUES (
    '11111111-1111-1111-1111-111111111111'::uuid,
    2, -- TRANSPORT (id = 2 in ref_movement_type)
    '2025-10-25T08:15:00Z',
    '2025-10-25T08:55:00Z',
    '2025-10-25',
    '{"type":"Point","coordinates":[37.6173,55.7558]}'::jsonb,
    '{"type":"Point","coordinates":[37.64,55.76]}'::jsonb,
    1,  -- HOME_RESIDENCE (id = 1 in ref_place_type)
    3,  -- WORKPLACE (id = 3 in ref_place_type)
    9,  -- METRO (id = 9 in ref_vehicle_type)
    62.00,
    3,
    1
) ON CONFLICT (uuid) DO NOTHING;

INSERT INTO movements(
    uuid,
    movement_type_id,
    departure_time,
    destination_time,
    day,
    departure_place,
    destination_place,
    departure_place_type_id,
    destination_place_type_id,
    vehicle_type_id,
    cost,
    waiting_time,
    seats_amount
) VALUES (
    '22222222-2222-2222-2222-222222222222'::uuid,
    1, -- ON_FOOT (id = 1 in ref_movement_type)
    '2025-10-26T09:00:00Z',
    '2025-10-26T09:20:00Z',
    '2025-10-26',
    '{"type":"Point","coordinates":[37.60,55.75]}'::jsonb,
    '{"type":"Point","coordinates":[37.61,55.76]}'::jsonb,
    1, -- HOME_RESIDENCE (id = 1 in ref_place_type)
    12, -- STORE_MARKET (id = 12 in ref_place_type)
    NULL,
    0.00,
    0,
    0
) ON CONFLICT (uuid) DO NOTHING;


-- Table for roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Table for scopes
CREATE TABLE IF NOT EXISTS scopes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Table for users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    locked BOOLEAN DEFAULT false,
    max_salary INTEGER,
    min_salary INTEGER,
    home_place JSONB,
    home_readable_place VARCHAR(255),
    gender VARCHAR(10) NOT NULL,
    last_login DATE DEFAULT CURRENT_DATE,
    creation_date DATE DEFAULT CURRENT_DATE
);

-- Association table: users_roles (Many-to-Many between users and roles)
CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Association table: users_scopes (Many-to-Many between users and scopes)
CREATE TABLE IF NOT EXISTS users_scopes (
    user_id BIGINT NOT NULL,
    scope_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, scope_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (scope_id) REFERENCES scopes(id) ON DELETE CASCADE
);

-- Association table: role_scopes (Many-to-Many between roles and scope strings)
CREATE TABLE IF NOT EXISTS role_scopes (
    role_id BIGINT NOT NULL,
    scope VARCHAR(255) NOT NULL,
    PRIMARY KEY (role_id, scope),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);




-- Insert test roles (only columns: id, name)
INSERT INTO roles (id, name) VALUES
    (101, 'ADMIN'),
    (102, 'MANAGER'),
    (103, 'USER'),
    (104, 'DEVELOPER')
ON CONFLICT (id) DO NOTHING;

-- Insert test scopes (only columns: id, name)
INSERT INTO scopes (id, name) VALUES
    (201, 'READ'),
    (202, 'WRITE'),
    (203, 'UPDATE'),
    (204, 'DELETE')
ON CONFLICT (id) DO NOTHING;

-- Insert test users with explicit long integer ids
INSERT INTO users (id, username, password, enabled, gender) VALUES
    (1001, 'test_employee@local.dev',  '$2a$10$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu', true, 'MALE'), -- password: qwerty
    (1002, 'test_manager@local.dev',   '$2a$10$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu', true, 'FEMALE'), -- password: qwerty
    (1003, 'power_admin@local.dev',    '$2a$10$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu', true, 'MALE')  -- password: qwerty
ON CONFLICT (id) DO NOTHING;

-- Assign roles to users (using explicit long integer ids)
INSERT INTO users_roles (user_id, role_id) VALUES
    (1001, 103), -- test_employee@local.dev => USER
    (1002, 102), -- test_manager@local.dev => MANAGER
    (1003, 101)  -- power_admin@local.dev => ADMIN
ON CONFLICT DO NOTHING;

-- Assign scopes to users (using explicit long integer ids)
INSERT INTO users_scopes (user_id, scope_id) VALUES
    (1001, 201), -- test_employee@local.dev => READ
    (1002, 201), -- test_manager@local.dev => READ
    (1002, 202), -- test_manager@local.dev => WRITE
    (1003, 201), -- power_admin@local.dev => READ
    (1003, 202), -- power_admin@local.dev => WRITE
    (1003, 203), -- power_admin@local.dev => UPDATE
    (1003, 204)  -- power_admin@local.dev => DELETE
ON CONFLICT DO NOTHING;

-- Example assignment of scopes directly to roles via role_scopes table
INSERT INTO role_scopes (role_id, scope) VALUES
    (101, 'READ'),
    (101, 'WRITE'),
    (101, 'UPDATE'),
    (101, 'DELETE'),
    (102, 'READ'),
    (102, 'WRITE'),
    (103, 'READ'),
    (104, 'READ')
ON CONFLICT DO NOTHING;
