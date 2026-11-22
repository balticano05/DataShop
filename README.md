# DataShop

Маркетплейс датасетов на Java 21: Javalin, Thymeleaf, Hibernate/JPA, Supabase (PostgreSQL + Storage) и Redis.

Подробная документация: [docs/PROJECT.md](docs/PROJECT.md). Проверка API через Bruno: [docs/BRUNO.md](docs/BRUNO.md).

База данных и медиа находятся в Supabase. Превью товаров хранятся прямо в PostgreSQL (`media_assets`), файлы датасетов — в приватном Supabase Storage. Локальный PostgreSQL не используется и не поднимается в Compose. Без `DATABASE_URL`, `SUPABASE_URL` и доступа к Storage (`SUPABASE_SERVICE_ROLE_KEY` либо `SUPABASE_PUBLISHABLE_KEY` + `SUPABASE_STORAGE_EMAIL`/`SUPABASE_STORAGE_PASSWORD`) приложение не запускается.

## Запуск через Docker

Создайте в корне проекта `.env` (он не коммитится). Минимум для запуска:

```dotenv
DATABASE_URL=postgresql://postgres.<project-ref>:<password>@aws-1-<region>.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_URL=https://<project-ref>.supabase.co
JWT_SECRET=<длинная случайная строка>

# Вариант 1: service role key.
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>

# Вариант 2: выделенный Supabase Auth user с RLS-политиками.
# SUPABASE_PUBLISHABLE_KEY=<publishable-key>
# SUPABASE_STORAGE_EMAIL=<storage-user-email>
# SUPABASE_STORAGE_PASSWORD=<storage-user-password>
```

`DATABASE_URL` — Session pooler URI из Supabase → Connect. Direct-хост `db.<ref>.supabase.co` работает только по IPv6 и из Docker недоступен. Остальные переменные и значения по умолчанию — в таблице ниже. Compose передаёт `.env` в контейнер и заменяет только `REDIS_URL` на адрес сервиса `redis`.

```bash
docker compose up --build
```

Приложение: <http://localhost:3000>  
Swagger UI: <http://localhost:3000/api/docs>  
OpenAPI: <http://localhost:3000/api/openapi.json> (алиас: `/api/docs-json`)

Остановка:

```bash
docker compose down
```

Удаление локального кэша Redis:

```bash
docker compose down -v
```

Данные в Supabase при этом сохраняются: Compose больше не содержит PostgreSQL и его тома.

## Локальный запуск

```bash
export DATABASE_URL='postgresql://postgres.<project-ref>:<password>@aws-1-<region>.pooler.supabase.com:5432/postgres?sslmode=require'
export SUPABASE_URL='https://<project-ref>.supabase.co'
export SUPABASE_SERVICE_ROLE_KEY='<service-role-key>'
# Или без service role key:
# export SUPABASE_PUBLISHABLE_KEY='<publishable-key>'
# export SUPABASE_STORAGE_EMAIL='<storage-user-email>'
# export SUPABASE_STORAGE_PASSWORD='<storage-user-password>'
export JWT_SECRET="$(openssl rand -base64 48)"
mvn package
java -jar target/datashop.jar
```

Требуются JDK 21, Maven 3.9+ и проект Supabase с PostgreSQL и Storage. Локальный PostgreSQL не нужен. Redis необязателен: без него используется in-memory кэш.

`DATABASE_URL` и `SUPABASE_URL` обязательны. Также нужен один из вариантов доступа к Storage: service role key либо publishable key с email и паролем выделенного Auth-пользователя. Иначе приложение завершается на старте с `IllegalStateException`.

## Миграции и данные

Схема базы данных обновляется автоматически через Liquibase при старте приложения. YAML-ченджлоги находятся в `src/main/resources/db/changelog`, а SQL-код лежит отдельно в `changes/sql` и `seed/sql` и разбит по ответственности: пользователи, товары, промокоды, корзины, заказы, медиа. Базовые миграции применяются всегда.

При `SEED_ENABLED=true` дополнительно применяется seed-миграция с демо-товарами и промокодом, а также создаётся администратор из `SEED_ADMIN_EMAIL` и `SEED_ADMIN_PASSWORD`. При `SEED_ENABLED=false` создаётся только схема, без демо-данных и администратора. Повторный запуск миграций безопасен.

Демо-товары, перенесённые из Neon, сохранили старые `file_public_id` вида `datashop/datasets/demo-*`. Самих файлов в Neon не было, поэтому для скачивания нужно загрузить датасет через админку заново.

## Переменные окружения

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `PORT` | Порт локального запуска; Docker публикует `3000` | `3000` |
| `DATABASE_URL` | Session pooler URI Supabase (`postgresql://...`) | — (обязательно) |
| `DB_POOL_SIZE` | Размер пула соединений | `10` |
| `REDIS_URL` | URL Redis | `redis://localhost:6379` |
| `CACHE_TTL_SECONDS` | TTL кэша каталога (60–120) | `90` |
| `JWT_SECRET` | Секрет подписи JWT | dev-значение |
| `JWT_EXPIRES_IN` | Время жизни JWT | `1h` |
| `COOKIE_SECURE` | Cookie только по HTTPS | `false` |
| `CORS_ORIGINS` | Разрешённые origins через запятую | `http://localhost:5173` |
| `RENDER_EXTERNAL_URL` | Публичный URL сервиса; Render задаёт автоматически | — |
| `SUPABASE_URL` | URL проекта Supabase | — (обязательно) |
| `SUPABASE_SERVICE_ROLE_KEY` | Server-side ключ для Supabase Storage (датасеты); не передавать в браузер | — (предпочтительно) |
| `SUPABASE_PUBLISHABLE_KEY` | Публичный ключ проекта для входа storage-пользователя | — |
| `SUPABASE_STORAGE_EMAIL` | Email выделенного Supabase Auth пользователя | — |
| `SUPABASE_STORAGE_PASSWORD` | Пароль выделенного Supabase Auth пользователя | — |
| `PREVIEW_MAX_BYTES` | Максимальный размер превью | `5242880` |
| `DATASET_MAX_BYTES` | Максимальный размер файла датасета | `52428800` |
| `SEED_ENABLED` | Seed-миграция с демо-данными и создание администратора | `true` |
| `SEED_ADMIN_EMAIL` | Email администратора | `admin@datashop.local` |
| `SEED_ADMIN_PASSWORD` | Пароль администратора | `Admin12345!` |

Для фронтенда прошлого семестра укажите его URL в `CORS_ORIGINS`.

### Превью и Supabase Storage

Превью товаров не используют Storage. Байты изображений лежат в таблице `media_assets` в Supabase PostgreSQL, а `products.images` содержит только внутренние пути вида `/media/assets/{uuid}`. Приложение отдаёт их через `GET /media/assets/{id}` с долгим кэшированием. Внешние сервисы для картинок не используются.

При старте приложение проверяет товары без превью и товары со старыми внешними или Storage-ссылками. Для них генерируются три SVG-превью, сохраняются в `media_assets`, а в `products.images` записываются внутренние пути. Удалённым товарам галерея очищается.

В Supabase Storage остаётся только приватный bucket `datasets` для платных файлов. С service role key приложение создаёт его автоматически. Если service role key недоступен, создайте bucket `datasets` и отдельного пользователя в Supabase Dashboard → Authentication → Users. Затем выдайте этому пользователю доступ к Storage через RLS-политики, ограниченные его `auth.uid()` и bucket'ом `datasets`. Готовый SQL и порядок настройки — в [docs/PROJECT.md](docs/PROJECT.md#доступ-к-supabase-storage-без-service-role-key).

## API

- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`
- `GET /api/products` — `page`, `limit`, `search`, `min_price`, `max_price`, `min_rating`, `sort_by`, `order`
- `POST /api/products`, `PATCH /api/products/{id}`, `DELETE /api/products/{id}` — только администратор
- `GET /api/cart`, `POST /api/cart/items`, `PATCH /api/cart/items/{id}`, `DELETE /api/cart/items/{id}`
- `POST /api/cart/apply-promo`, `DELETE /api/cart/apply-promo`
- `POST /api/orders/checkout`, `GET /api/orders`, `GET /api/orders/{id}`
- `POST /api/admin/media/preview` — загрузка превью в Supabase PostgreSQL
- `POST /api/admin/media/dataset/{productId}` — загрузка датасета в приватный Supabase Storage
- `GET /media/assets/{id}` — публичная выдача превью из PostgreSQL
- `GET /api/downloads/{productId}` — временная приватная ссылка Supabase Storage

Авторизация: `Authorization: Bearer <token>`.

## Проверка API через Bruno

В `docs/bruno/DataShop` лежит готовая stateful-коллекция: она покрывает все REST-эндпоинты, включая RBAC, фильтры каталога, кэш, корзину, промокод, checkout, превью в PostgreSQL, Supabase Storage и скачивание датасета.

```bash
cd docs/bruno/DataShop
npx @usebruno/cli run --env Local
```

Подробная инструкция, список запросов и разбор ошибок — в [docs/BRUNO.md](docs/BRUNO.md).

## Деплой на Render

[Deploy to Render](https://render.com/deploy?repo=https://github.com/balticano05/DataShop)

`render.yaml` создаёт web-сервис из `Dockerfile`, PostgreSQL и Storage берутся из Supabase (`DATABASE_URL`, `SUPABASE_URL` и доступ к Storage для датасетов), Redis — Render Key Value. При создании сервиса заполните `SUPABASE_SERVICE_ROLE_KEY` либо `SUPABASE_PUBLISHABLE_KEY` + `SUPABASE_STORAGE_EMAIL` + `SUPABASE_STORAGE_PASSWORD`: без доступа к Storage приложение не запустится. После деплоя Swagger доступен по адресу `https://<service>.onrender.com/api/docs`.

## Тесты

```bash
mvn test
```

Интеграционная проверка API: `37` запросов и `52` проверки Bruno, см. [docs/BRUNO.md](docs/BRUNO.md).
