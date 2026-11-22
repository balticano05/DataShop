-- DataShop schema: products and catalog indexes.

CREATE TABLE IF NOT EXISTS products (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    title varchar(255) NOT NULL,
    description text NOT NULL,
    price numeric(10, 2) NOT NULL,
    rating numeric(2, 1) NOT NULL DEFAULT 0,
    images text NOT NULL DEFAULT '[]',
    stock_quantity integer NOT NULL DEFAULT 0,
    file_public_id varchar(255),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,
    CONSTRAINT chk_products_stock_quantity CHECK (stock_quantity >= 0)
);

CREATE INDEX IF NOT EXISTS idx_products_title ON products (title);
CREATE INDEX IF NOT EXISTS idx_products_rating ON products (rating);
CREATE INDEX IF NOT EXISTS idx_products_price ON products (price);
