-- Seed data: demo promo code.

INSERT INTO promo_codes (id, code, discount_percentage, is_active, created_at, updated_at)
SELECT gen_random_uuid(), 'SAVE10', 10, true, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM promo_codes WHERE code = 'SAVE10');
