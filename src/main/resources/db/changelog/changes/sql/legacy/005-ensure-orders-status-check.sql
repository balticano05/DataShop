-- Legacy migration: 002-ensure-orders-status-check.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = current_schema()
          AND table_name = 'orders'
          AND constraint_name = 'chk_orders_status'
    ) THEN
        ALTER TABLE orders ADD CONSTRAINT chk_orders_status
            CHECK (status IN ('pending', 'paid', 'cancelled', 'refunded'));
    END IF;
END $$;
