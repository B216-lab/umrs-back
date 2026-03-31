CREATE TABLE IF NOT EXISTS movements_form_respondent_keys
(
    id          BIGSERIAL PRIMARY KEY,
    key_value   VARCHAR(255)                NOT NULL UNIQUE,
    user_id     BIGINT REFERENCES users (id) ON DELETE SET NULL,
    active      BOOLEAN                     NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ                 NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_movements_form_respondent_keys_user_id
    ON movements_form_respondent_keys (user_id);

ALTER TABLE movements_form_submissions
    ADD COLUMN IF NOT EXISTS respondent_key_id BIGINT REFERENCES movements_form_respondent_keys (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_movements_form_submissions_respondent_key_id
    ON movements_form_submissions (respondent_key_id);
