ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash varchar(120);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role varchar(16);

UPDATE users
SET password_hash = '$2a$10$P1e95wD7f7YQnYvE8g9ZFew3qVZC5oWcKJg1K4v7D2in1JmG9dB9S'
WHERE password_hash IS NULL;

UPDATE users
SET role = 'RIDER'
WHERE role IS NULL;

ALTER TABLE users
  ALTER COLUMN password_hash SET NOT NULL,
  ALTER COLUMN role SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_role_chk') THEN
    ALTER TABLE users
      ADD CONSTRAINT users_role_chk CHECK (role IN ('RIDER','DRIVER','ADMIN'));
  END IF;
END$$;
