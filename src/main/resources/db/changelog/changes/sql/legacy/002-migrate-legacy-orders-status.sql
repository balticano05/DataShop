-- Legacy migration: 002-migrate-legacy-orders-status.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'orders'
          AND column_name = 'status'
          AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE orders ALTER COLUMN status DROP DEFAULT;
        ALTER TABLE orders ALTER COLUMN status TYPE varchar(20) USING status::text;
        ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'pending';
    END IF;
END $$;
