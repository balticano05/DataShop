-- Legacy migration: 002-migrate-legacy-users-role.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'users'
          AND column_name = 'role'
          AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE users ALTER COLUMN role DROP DEFAULT;
        ALTER TABLE users ALTER COLUMN role TYPE varchar(20) USING role::text;
        ALTER TABLE users ALTER COLUMN role SET DEFAULT 'customer';
    END IF;
END $$;
