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


