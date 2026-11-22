-- Legacy migration: 002-migrate-legacy-products-images.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'products'
          AND column_name = 'images'
          AND data_type = 'ARRAY'
    ) THEN
        ALTER TABLE products ALTER COLUMN images DROP DEFAULT;
        ALTER TABLE products ALTER COLUMN images TYPE text USING to_json(images)::text;
        ALTER TABLE products ALTER COLUMN images SET DEFAULT '[]';
    END IF;
END $$;
