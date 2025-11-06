-- Enable PostGIS extension if not present
CREATE EXTENSION IF NOT EXISTS postgis;

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

-- Seed movement types
INSERT INTO ref_movement_type(code, description_ru) VALUES
    ('ON_FOOT', 'пешком'),
    ('TRANSPORT', 'транспорт')
ON CONFLICT (code) DO NOTHING;

-- Seed place types
INSERT INTO ref_place_type(code, description_ru) VALUES
    ('HOME_RESIDENCE','дом - место жительства'),
    ('FRIENDS_RELATIVES_HOME','дом друзей / родственников'),
    ('WORKPLACE','работа / рабочее место'),
    ('WORK_BUSINESS_TRIP','работа - служебная поездка'),
    ('DAYCARE_CENTER','детский сад'),
    ('SCHOOL','школа'),
    ('COLLEGE_TECHNICAL_SCHOOL','колледж / техникум / училище'),
    ('UNIVERSITY_INSTITUTE','университет / институт'),
    ('HOSPITAL_CLINIC','больница / поликлиника'),
    ('CULTURAL_INSTITUTION','учреждение культуры (музей, театр, цирк, библиотека и т.п.)'),
    ('SPORT_FITNESS','спорт / фитнес'),
    ('STORE_MARKET','магазин / рынок'),
    ('SHOPPING_ENTERTAINMENT_CENTER','торгово - развлекательный центр'),
    ('RESTAURANT_CAFE','ресторан / кафе / пункт общественного питания'),
    ('SUBURB','пригород'),
    ('OTHER','другое')
ON CONFLICT (code) DO NOTHING;

-- Seed vehicle types
INSERT INTO ref_vehicle_type(code, description_ru) VALUES
    ('BICYCLE','велосипед'),
    ('INDIVIDUAL_MOBILITY','средства индивидуальной мобильности (самокат и пр.)'),
    ('BUS','автобус'),
    ('SHUTTLE_TAXI','маршрутное такси'),
    ('TRAM','трамвай'),
    ('PRIVATE_CAR','личный автомобиль'),
    ('TROLLEYBUS','троллейбус'),
    ('SUBURBAN_TRAIN','электричка'),
    ('METRO','метро'),
    ('TAXI','такси'),
    ('CAR_SHARING','каршеринг'),
    ('CITY_BIKE_RENTAL','городской велопрокат'),
    ('SERVICE','служебный транспорт')
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

-- Seed sample movements
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
    (SELECT id FROM ref_movement_type WHERE code = 'TRANSPORT'),
    '2025-10-25T08:15:00Z',
    '2025-10-25T08:55:00Z',
    '2025-10-25',
    '{"type":"Point","coordinates":[37.6173,55.7558]}'::jsonb,
    '{"type":"Point","coordinates":[37.64,55.76]}'::jsonb,
    (SELECT id FROM ref_place_type WHERE code = 'HOME_RESIDENCE'),
    (SELECT id FROM ref_place_type WHERE code = 'WORKPLACE'),
    (SELECT id FROM ref_vehicle_type WHERE code = 'METRO'),
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
    (SELECT id FROM ref_movement_type WHERE code = 'ON_FOOT'),
    '2025-10-26T09:00:00Z',
    '2025-10-26T09:20:00Z',
    '2025-10-26',
    '{"type":"Point","coordinates":[37.60,55.75]}'::jsonb,
    '{"type":"Point","coordinates":[37.61,55.76]}'::jsonb,
    (SELECT id FROM ref_place_type WHERE code = 'HOME_RESIDENCE'),
    (SELECT id FROM ref_place_type WHERE code = 'STORE_MARKET'),
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
INSERT INTO users (id, username, password, enabled) VALUES
    (1001, 'test_employee@local.dev',  '$2a$10$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu', true), -- password: qwerty
    (1002, 'test_manager@local.dev',   '$2a$10$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu', true), -- password: qwerty
    (1003, 'power_admin@local.dev',    '$2a$10$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu', true)  -- password: qwerty
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



