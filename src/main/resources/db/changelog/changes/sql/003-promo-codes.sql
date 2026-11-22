-- DataShop schema: promo codes.

CREATE TABLE IF NOT EXISTS promo_codes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code varchar(64) NOT NULL,
    discount_percentage integer NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_promo_codes_code UNIQUE (code),
    CONSTRAINT chk_promo_discount CHECK (discount_percentage BETWEEN 0 AND 100)
);
