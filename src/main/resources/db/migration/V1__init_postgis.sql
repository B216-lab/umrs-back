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


