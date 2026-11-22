-- Seed data: demo product catalog.

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Акции Tesla 2019–2024', 'Дневные котировки TSLA с объёмами и дивидендами.', 1499.0, 4.8, '[]', 25, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Акции Tesla 2019–2024');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Крипто-пары BTC/ETH 1 минута', 'Минутные свечи биржи за три года, 12 инструментов.', 2499.0, 4.9, '[]', 15, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Крипто-пары BTC/ETH 1 минута');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Рынок недвижимости США', 'Цены, ставки и объёмы сделок по округам за 10 лет.', 3990.0, 4.5, '[]', 10, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Рынок недвижимости США');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Логи веб-сервера Nginx', 'Размеченные access-логи за месяц, 5 млн строк.', 899.0, 4.2, '[]', 40, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Логи веб-сервера Nginx');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Отзывы Amazon (электроника)', 'Обезличенные отзывы и оценки, 500k записей.', 1290.0, 4.4, '[]', 30, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Отзывы Amazon (электроника)');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Погода по 100 городам', 'Почасовые наблюдения: температура, давление, ветер.', 690.0, 4.1, '[]', 50, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Погода по 100 городам');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Авиарейсы и задержки', 'Расписание и фактические времена вылета за 2024.', 2190.0, 4.6, '[]', 12, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Авиарейсы и задержки');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Вакансии IT (HeadHunter)', 'Названия, грейды и вилки зарплат, 300k вакансий.', 1990.0, 4.7, '[]', 18, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Вакансии IT (HeadHunter)');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Медицинские снимки (демо)', 'Анонимизированный набор рентген-снимков с разметкой.', 4990.0, 4.9, '[]', 6, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Медицинские снимки (демо)');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Транзакции ритейла', 'Чеки супермаркета: товары, суммы, время покупки.', 1590.0, 4.3, '[]', 22, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Транзакции ритейла');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Курсы валют ЦБ', 'Ежедневные курсы 30 валют за 15 лет.', 490.0, 4.0, '[]', 60, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Курсы валют ЦБ');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Соцсеть: граф друзей', 'Обезличенный граф связей, 1 млн рёбер.', 2790.0, 4.5, '[]', 9, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Соцсеть: граф друзей');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Музыкальные треки и жанры', 'Аудио-фичи и жанровая разметка 200k треков.', 1890.0, 4.6, '[]', 14, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Музыкальные треки и жанры');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Дорожный трафик (датчики)', 'Интенсивность потока по 500 датчикам за год.', 3290.0, 4.4, '[]', 8, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Дорожный трафик (датчики)');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Футбольная статистика', 'Матчи, голы и xG по 20 лигам за 10 сезонов.', 2590.0, 4.8, '[]', 11, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Футбольная статистика');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Энергопотребление домохозяйств', 'Почасовые показания счётчиков, 200 домов.', 1190.0, 4.2, '[]', 20, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Энергопотребление домохозяйств');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Опросы общественного мнения', 'Анонимизированные ответы 50k респондентов.', 990.0, 4.1, '[]', 35, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Опросы общественного мнения');

INSERT INTO products (id, title, description, price, rating, images, stock_quantity, file_public_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Логи приложений (APM)', 'Трассировки и ошибки распределённой системы.', 3490.0, 4.7, '[]', 7, NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Логи приложений (APM)');
