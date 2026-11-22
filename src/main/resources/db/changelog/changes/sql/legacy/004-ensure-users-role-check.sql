-- Legacy migration: 002-ensure-users-role-check.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = current_schema()
          AND table_name = 'users'
          AND constraint_name = 'chk_users_role'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('admin', 'customer'));
    END IF;
END $$;
