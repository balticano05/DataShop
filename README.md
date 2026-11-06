# DataShop

Маркетплейс датасетов на Java 21: Javalin, Thymeleaf, Hibernate/JPA, PostgreSQL, Redis и Cloudinary.

## Запуск через Docker

```bash
cp .env.example .env
docker compose up --build
```

Приложение: <http://localhost:3000>  
Swagger UI: <http://localhost:3000/api/docs>  
OpenAPI: <http://localhost:3000/api/docs-json>

Остановка:

```bash
docker compose down
```

Удаление данных:

```bash
docker compose down -v
```

## Локальный запуск

```bash
cd backend-java
mvn package
java -jar target/datashop.jar
```

Требуются JDK 21, Maven 3.9+, PostgreSQL 16. Redis необязателен: без него используется in-memory кэш.

## Переменные окружения

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `PORT` | Порт приложения | `3000` |
| `DATABASE_URL` | PostgreSQL URL (`postgres://` или JDBC) | `jdbc:postgresql://localhost:5432/shop` |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Параметры PostgreSQL, если `DATABASE_URL` не задан | `localhost`, `5432`, `shop` |
| `DB_USER`, `DB_PASSWORD` | Учётные данные PostgreSQL, если они не в `DATABASE_URL` | `postgres`, `postgres` |
| `DB_POOL_SIZE` | Размер пула соединений | `10` |
| `REDIS_URL` | URL Redis | `redis://localhost:6379` |
| `CACHE_TTL_SECONDS` | TTL кэша каталога (60–120) | `90` |
| `JWT_SECRET` | Секрет подписи JWT | dev-значение |
| `JWT_EXPIRES_IN` | Время жизни JWT | `1h` |
| `COOKIE_SECURE` | Cookie только по HTTPS | `false` |
| `CORS_ORIGINS` | Разрешённые origins через запятую | `http://localhost:5173` |
| `PUBLIC_BASE_URL` | Базовый URL для ссылок на скачивание | `http://localhost:<PORT>` |
| `RENDER_EXTERNAL_URL` | Публичный URL сервиса; Render задаёт автоматически | — |
| `CLOUDINARY_URL` | Cloudinary URL; без него файлы хранятся локально | — |
| `UPLOADS_DIR` | Каталог локальных загрузок | `uploads` |
| `PREVIEW_MAX_BYTES` | Максимальный размер превью | `5242880` |
| `DATASET_MAX_BYTES` | Максимальный размер файла датасета | `52428800` |
| `SEED_ENABLED` | Демо-данные при старте | `true` |
| `SEED_ADMIN_EMAIL` | Email администратора | `admin@datashop.local` |
| `SEED_ADMIN_PASSWORD` | Пароль администратора | `Admin12345!` |
| `POSTGRES_USER` | Пользователь PostgreSQL для compose | `postgres` |
| `POSTGRES_PASSWORD` | Пароль PostgreSQL для compose | `postgres` |
| `POSTGRES_DB` | База PostgreSQL для compose | `shop` |

Для фронтенда прошлого семестра укажите его URL в `CORS_ORIGINS`.

## API

- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`
- `GET /api/products` — `page`, `limit`, `search`, `min_price`, `max_price`, `min_rating`, `sort_by`, `order`
- `POST /api/products`, `PATCH /api/products/{id}`, `DELETE /api/products/{id}` — только администратор
- `GET /api/cart`, `POST /api/cart/items`, `PATCH /api/cart/items/{id}`, `DELETE /api/cart/items/{id}`
- `POST /api/cart/apply-promo`, `DELETE /api/cart/apply-promo`
- `POST /api/orders/checkout`, `GET /api/orders`, `GET /api/orders/{id}`
- `POST /api/admin/media/preview`, `POST /api/admin/media/dataset/{productId}`
- `GET /api/downloads/{productId}`, `GET /downloads/{token}`

Авторизация: `Authorization: Bearer <token>`.

## Деплой на Render

[Deploy to Render](https://render.com/deploy?repo=https://github.com/balticano05/DataShop)

`render.yaml` создаёт web-сервис из `backend-java/Dockerfile`, PostgreSQL берётся из Neon (`DATABASE_URL`), Redis — Render Key Value. После деплоя Swagger доступен по адресу `https://<service>.onrender.com/api/docs`.

## Тесты

```bash
cd backend-java
mvn test
```
