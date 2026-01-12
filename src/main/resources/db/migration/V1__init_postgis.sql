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

-- Create movements table (JSONB for places, FKs to reference tables)
CREATE TABLE IF NOT EXISTS movements
(
    id                           BIGSERIAL PRIMARY KEY,
    movement_type_id             BIGINT NOT NULL REFERENCES ref_movement_type (id),
    departure_time               TIMESTAMPTZ,
    destination_time             TIMESTAMPTZ,
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
    seats_amount                 INTEGER,
    comment                      VARCHAR(2000),
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


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

-- Table for movements form submissions (stores user data from form, can be anonymous or linked to user)
CREATE TABLE IF NOT EXISTS movements_form_submissions
(
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT REFERENCES users (id) ON DELETE SET NULL,
    birthday                DATE,
    gender                  VARCHAR(10),
    social_status_id        BIGINT REFERENCES social_statuses (id),
    transport_cost_min      INTEGER,
    transport_cost_max      INTEGER,
    income_min              INTEGER,
    income_max              INTEGER,
    home_address            JSONB,
    home_readable_address   VARCHAR(512),
    movements_date          DATE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_movements_form_submissions_user_id ON movements_form_submissions (user_id);

-- Add foreign key column to movements table linking to movements_form_submissions
-- Note: Since this is V1 migration, there should be no existing data, so we can add it as NOT NULL
ALTER TABLE movements
ADD COLUMN IF NOT EXISTS movements_form_submission_id BIGINT NOT NULL;

ALTER TABLE movements
ADD CONSTRAINT fk_movements_form_submission 
FOREIGN KEY (movements_form_submission_id) 
REFERENCES movements_form_submissions (id) 
ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_movements_movements_form_submission_id ON movements (movements_form_submission_id);

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
