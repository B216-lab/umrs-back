DROP TABLE IF EXISTS "SequelizeMeta";

DROP TABLE IF EXISTS "key-auth";

DROP TABLE IF EXISTS "user";

DROP TABLE IF EXISTS "questionnaire_status";

DELETE FROM "questionnaire"
WHERE "name" ILIKE '%тест%'
   OR "name" ILIKE '%удалить%';

ALTER TABLE questionnaire DROP COLUMN IF EXISTS currency;

ALTER TABLE questionnaire ADD COLUMN IF NOT EXISTS new_user_id BIGINT;

-- transportationCosts лучше считать на основе введенных перемещений
-- city бесполезная колонка


-- Step 3: Create users from questionnaire data and update new_user_id
DO $$
DECLARE
    q_record RECORD;
    new_username VARCHAR(255);
    new_user_id BIGINT;
    user_password VARCHAR(255) := '$2a$10$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu'; -- password: qwerty
    user_gender VARCHAR(10);
    username_counter INTEGER;
BEGIN
    -- Iterate through all questionnaire records
    FOR q_record IN SELECT id, name, gender FROM questionnaire LOOP
        -- Determine username
        IF q_record.name IS NULL OR TRIM(q_record.name) = '' THEN
            -- Generate unique username with random digits
            username_counter := 1;
            LOOP
                new_username := 'unknown' || LPAD((FLOOR(RANDOM() * 1000000)::INTEGER)::TEXT, 6, '0');
                -- Check if username already exists
                EXIT WHEN NOT EXISTS (SELECT 1 FROM users WHERE username = new_username);
                username_counter := username_counter + 1;
                -- Prevent infinite loop
                IF username_counter > 1000 THEN
                    new_username := 'unknown' || EXTRACT(EPOCH FROM NOW())::BIGINT::TEXT;
                    EXIT;
                END IF;
            END LOOP;
        ELSE
            -- Use name as base for username, ensure uniqueness
            new_username := TRIM(q_record.name);
            -- Remove or replace invalid characters for username
            new_username := REGEXP_REPLACE(new_username, '[^a-zA-Z0-9_@.-]', '_', 'g');
            -- If sanitized username is empty, fall back to unknown pattern
            IF new_username IS NULL OR TRIM(new_username) = '' THEN
                new_username := 'unknown' || LPAD((FLOOR(RANDOM() * 1000000)::INTEGER)::TEXT, 6, '0');
                -- Check if username already exists
                WHILE EXISTS (SELECT 1 FROM users WHERE username = new_username) LOOP
                    new_username := 'unknown' || LPAD((FLOOR(RANDOM() * 1000000)::INTEGER)::TEXT, 6, '0');
                END LOOP;
            -- Check if username already exists and append suffix if needed
            ELSIF EXISTS (SELECT 1 FROM users WHERE username = new_username) THEN
                username_counter := 1;
                LOOP
                    new_username := TRIM(q_record.name) || '_' || username_counter::TEXT;
                    new_username := REGEXP_REPLACE(new_username, '[^a-zA-Z0-9_@.-]', '_', 'g');
                    EXIT WHEN NOT EXISTS (SELECT 1 FROM users WHERE username = new_username);
                    username_counter := username_counter + 1;
                    IF username_counter > 10000 THEN
                        new_username := TRIM(q_record.name) || '_' || EXTRACT(EPOCH FROM NOW())::BIGINT::TEXT;
                        new_username := REGEXP_REPLACE(new_username, '[^a-zA-Z0-9_@.-]', '_', 'g');
                        EXIT;
                    END IF;
                END LOOP;
            END IF;
        END IF;

        -- Map gender from questionnaire to Gender enum (MALE or FEMALE)
        -- Handle various possible gender values
        IF q_record.gender IS NULL THEN
            user_gender := 'MALE'; -- Default to MALE if null
        ELSE
            -- Normalize gender value
            CASE UPPER(TRIM(q_record.gender))
                WHEN 'МУЖСКОЙ' THEN user_gender := 'MALE';
                WHEN 'ЖЕНСКИЙ' THEN user_gender := 'FEMALE';
                ELSE user_gender := 'MALE'; -- Default to MALE for unknown values
            END CASE;
        END IF;

        -- Map finantialSituation to minSalary and maxSalary (in thousands)
        IF q_record."finantialSituation" IS NULL OR TRIM(q_record."finantialSituation") = '' THEN
            user_min_salary := NULL;
            user_max_salary := NULL;
        ELSE
            CASE TRIM(q_record."finantialSituation")
                WHEN 'нет заработка' THEN
                    user_min_salary := 0;
                    user_max_salary := 0;
                WHEN 'до 10 тысяч рублей' THEN
                    user_min_salary := 0;
                    user_max_salary := 10;
                WHEN 'от 10 до 25 тысяч рублей' THEN
                    user_min_salary := 10;
                    user_max_salary := 25;
                WHEN 'от 45 до 65 тысяч рублей' THEN
                    user_min_salary := 45;
                    user_max_salary := 65;
                WHEN 'от 65 до 90 тысяч рублей' THEN
                    user_min_salary := 65;
                    user_max_salary := 90;
                WHEN 'от 90 до 120 тысяч рублей' THEN
                    user_min_salary := 90;
                    user_max_salary := 120;
                WHEN 'от 120 тысяч рублей и выше' THEN
                    user_min_salary := 120;
                    user_max_salary := NULL; -- Open-ended upper bound
                ELSE
                    user_min_salary := NULL;
                    user_max_salary := NULL;
            END CASE;
        END IF;
        
        -- Map address to homeReadablePlace
        IF q_record.address IS NULL OR TRIM(q_record.address) = '' THEN
            user_home_readable_place := NULL;
        ELSE
            user_home_readable_place := TRIM(q_record.address);
        END IF;

        -- Map coordinatesAddress to homePlace
        IF q_record."coordinatesAddress" IS NULL OR TRIM(q_record."coordinatesAddress") = '' THEN
            user_home_place := NULL;
        ELSE
            user_home_place := q_record."coordinatesAddress"::jsonb;
        END IF;
        
        -- Map socialStatus to a known code from the social_statuses table
        IF q_record."socialStatus" IS NULL OR TRIM(q_record."socialStatus") = '' THEN
            user_social_status_code := NULL;
        ELSE
            CASE TRIM(q_record."socialStatus")
                WHEN 'работающий' THEN
                    user_social_status_code := 'WORKING';
                WHEN 'школьник' THEN
                    user_social_status_code := 'STUDENT';
                WHEN 'студент' THEN
                    user_social_status_code := 'UNIVERSITY_STUDENT';
                WHEN 'пенсионер по возрасту' THEN
                    user_social_status_code := 'PENSIONER';
                WHEN 'человек c ограниченными возможностями' THEN
                    user_social_status_code := 'PERSON_WITH_DISABILITIES';
                WHEN 'безработный' THEN
                    user_social_status_code := 'UNEMPLOYED';
                WHEN 'домохозяйка' THEN
                    user_social_status_code := 'HOUSEWIFE';
                WHEN 'временно нетрудящийся (декретный отпуск, отпуск по уходу за ребенком)' THEN
                    user_social_status_code := 'TEMPORARILY_UNEMPLOYED';
                ELSE
                    user_social_status_code := NULL;
            END CASE;
        END IF;
        
        -- Disable the user by setting enabled to false
        user_enabled := false;

        -- Insert new user
        INSERT INTO users (username, password, enabled, locked, gender, last_login, creation_date)
        VALUES (new_username, user_password, true, false, user_gender::text, CURRENT_DATE, CURRENT_DATE)
        RETURNING id INTO new_user_id;

        -- Update questionnaire with new_user_id
        UPDATE questionnaire
        SET new_user_id = new_user_id
        WHERE id = q_record.id;

    END LOOP;
END $$;

-- Step 4: Remove duplicate users and update questionnaire references
-- This handles cases where the same user might have been created multiple times
-- Compares users based on multiple columns (gender, salary, home place, etc.) to identify duplicates
DO $$
DECLARE
    duplicate_record RECORD;
    kept_user_id BIGINT;
    duplicate_user_id BIGINT;
    all_duplicate_ids BIGINT[];
BEGIN
    -- Find duplicate users by comparing multiple columns that should be unique per person
    -- Group by: gender, min_salary, max_salary, home_readable_place, home_place
    -- Users with matching values in these fields are considered duplicates
    -- PostgreSQL naturally groups NULL values together in GROUP BY
    FOR duplicate_record IN 
        SELECT 
            gender,
            min_salary,
            max_salary,
            home_readable_place,
            home_place,
            MIN(id) as kept_id,
            array_agg(id ORDER BY id) as all_ids
        FROM users
        WHERE id NOT IN (1001, 1002, 1003) -- Exclude test users
        GROUP BY gender, min_salary, max_salary, home_readable_place, home_place
        HAVING COUNT(*) > 1
    LOOP
        kept_user_id := duplicate_record.kept_id;
        all_duplicate_ids := duplicate_record.all_ids;
        
        -- Update all questionnaire records pointing to duplicate users to point to kept user
        UPDATE questionnaire
        SET new_user_id = kept_user_id
        WHERE new_user_id = ANY(all_duplicate_ids)
          AND new_user_id != kept_user_id;
        
        -- Delete duplicate users (keeping the one with lowest id)
        DELETE FROM users
        WHERE id = ANY(all_duplicate_ids)
          AND id != kept_user_id
          AND id NOT IN (1001, 1002, 1003); -- Exclude test users
    END LOOP;
    
    -- Also handle cases where some fields might be NULL but users are still duplicates
    -- Compare users that have matching non-NULL fields
    FOR duplicate_record IN
        WITH user_comparisons AS (
            SELECT 
                u1.id as id1,
                u2.id as id2
            FROM users u1
            INNER JOIN users u2 ON u1.id < u2.id
            WHERE u1.id NOT IN (1001, 1002, 1003)
              AND u2.id NOT IN (1001, 1002, 1003)
              -- Match on gender (required field)
              AND u1.gender = u2.gender
              -- Match on salary fields (both NULL or both same value)
              AND (u1.min_salary IS NULL AND u2.min_salary IS NULL OR u1.min_salary = u2.min_salary)
              AND (u1.max_salary IS NULL AND u2.max_salary IS NULL OR u1.max_salary = u2.max_salary)
              -- Match on home_readable_place (both NULL or both same value)
              AND (u1.home_readable_place IS NULL AND u2.home_readable_place IS NULL 
                   OR u1.home_readable_place = u2.home_readable_place)
              -- Match on home_place JSONB (both NULL or both same)
              AND (u1.home_place IS NULL AND u2.home_place IS NULL 
                   OR u1.home_place::text = u2.home_place::text)
              -- Created within a reasonable time window (same migration run)
              AND ABS(EXTRACT(EPOCH FROM (u1.creation_date - u2.creation_date))) < 300 -- Within 5 minutes
        )
        SELECT DISTINCT
            LEAST(id1, id2) as kept_id,
            GREATEST(id1, id2) as duplicate_id
        FROM user_comparisons
        WHERE id1 IN (SELECT new_user_id FROM questionnaire WHERE new_user_id IS NOT NULL)
           OR id2 IN (SELECT new_user_id FROM questionnaire WHERE new_user_id IS NOT NULL)
    LOOP
        kept_user_id := duplicate_record.kept_id;
        duplicate_user_id := duplicate_record.duplicate_id;
        
        -- Update all questionnaire references from duplicate to kept user
        UPDATE questionnaire
        SET new_user_id = kept_user_id
        WHERE new_user_id = duplicate_user_id;
        
        -- Delete the duplicate user (only if no longer referenced)
        IF NOT EXISTS (SELECT 1 FROM questionnaire WHERE new_user_id = duplicate_user_id) THEN
            DELETE FROM users
            WHERE id = duplicate_user_id;
        END IF;
    END LOOP;
END $$;

-- Step 5: Add foreign key constraint for new_user_id (optional, but recommended)
ALTER TABLE questionnaire ADD CONSTRAINT fk_questionnaire_user 
    FOREIGN KEY (new_user_id) REFERENCES users(id) ON DELETE RESTRICT;

