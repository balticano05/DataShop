# DataShop — документация проекта

Версия: 1.0.0 · Дата: 09.11.2026

## Содержание

1. [О проекте](#1-о-проекте)
2. [Что сделано](#2-что-сделано)
3. [Стек](#3-стек)
4. [Структура репозитория](#4-структура-репозитория)
5. [Быстрый старт через Docker](#5-быстрый-старт-через-docker)
6. [Локальный запуск](#6-локальный-запуск)
7. [Конфигурация](#7-конфигурация)
8. [API и Swagger](#8-api-и-swagger)
9. [Примеры запросов](#9-примеры-запросов)
10. [Тесты](#10-тесты)
11. [Docker и инфраструктура](#11-docker-и-инфраструктура)
12. [Деплой на Render](#12-деплой-на-render)
13. [Git Flow](#13-git-flow)
14. [Безопасность](#14-безопасность)
15. [Ограничения и риски](#15-ограничения-и-риски)
16. [Чек-лист перед сдачей](#16-чек-лист-перед-сдачей)

## 1. О проекте

DataShop — учебный маркетплейс датасетов. Пользователь может зарегистрироваться, просматривать каталог, фильтровать товары, добавлять их в корзину, применять промокод, оформлять заказ и скачивать купленные датасеты. Администратор управляет каталогом и загружает файлы датасетов.

Проверка REST API через Bruno: [BRUNO.md](BRUNO.md).

Приложение отдаёт REST API и одновременно имеет серверный веб-интерфейс на Thymeleaf. Отдельный SPA-фронтенд не требуется.

База данных и медиа находятся в Supabase. Превью товаров хранятся прямо в PostgreSQL (`media_assets`), файлы датасетов — в приватном Supabase Storage. Локальный PostgreSQL не используется и не поднимается в Compose. Без `DATABASE_URL`, `SUPABASE_URL` и доступа к Storage (`SUPABASE_SERVICE_ROLE_KEY` либо `SUPABASE_PUBLISHABLE_KEY` + `SUPABASE_STORAGE_EMAIL`/`SUPABASE_STORAGE_PASSWORD`) приложение не запускается.

## 2. Что сделано

### Backend

- Реализован HTTP-сервер на Java 21 и Javalin 6.
- Добавлены REST-контроллеры, JSON-сериализация, валидация входных данных и единый формат ошибок.
- Настроены статические файлы, шаблоны Thymeleaf, CORS и security-заголовки.
- Реализованы health- и readiness-проверки.

### Доменная модель

- `User` — покупатель или администратор.
- `Product` — товар с ценой, рейтингом, остатком и галереей изображений.
- `MediaAsset` — бинарные превью товаров (`bytea`) в Supabase PostgreSQL.
- `Cart`, `CartItem` — корзина пользователя.
- `PromoCode` — промокод со скидкой в процентах.
- `Order`, `OrderItem` — заказ и снимок позиций на момент покупки.
- Для товаров используется мягкое удаление, для заказов — история.
- Схема БД создаётся и обновляется автоматически YAML-ченджлогами Liquibase при старте.
- SQL-код вынесен в отдельные файлы `db/changelog/changes/sql` и `db/changelog/seed/sql` и разбит по ответственности.

### Авторизация и роли

- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`.
- В API JWT передаётся в заголовке `Authorization: Bearer <token>`.
- В веб-интерфейсе токен хранится в HttpOnly cookie.
- Пароли хешируются Argon2id.
- Роли `customer` и `admin`; изменение каталога доступно только администратору.
- Для форм веб-интерфейса используется CSRF-защита.
- Ограничение частоты запросов: регистрация и вход — 5 запросов в минуту, checkout — 10 запросов в минуту.

### Каталог и кэширование

- `GET /api/products` поддерживает пагинацию, поиск, фильтры и сортировку.
- Метаданные ответа: `total_items`, `total_pages`, `current_page`, `limit`.
- Кэш каталога хранится в Redis и инвалидируется при создании, изменении и удалении товара, а также при загрузке файла датасета.
- TTL кэша по умолчанию — 90 секунд; рекомендуется диапазон 60–120.
- Ключ кэша строится по нормализованным параметрам запроса.
- При недоступном Redis приложение автоматически переходит на in-memory кэш.
- Ответ содержит заголовок `X-Cache: HIT` или `X-Cache: MISS`.

### Корзина и промокоды

- Получение корзины, добавление, изменение количества и удаление позиций.
- Количество позиции — от 1 до 99, проверяется наличие на складе.
- Промокод `SAVE10` даёт скидку 10%.
- `POST /api/cart/apply-promo` применяет промокод, `DELETE /api/cart/apply-promo` сбрасывает его.
- Итоги корзины рассчитываются с округлением до копеек.

### Заказы и загрузки

- `POST /api/orders/checkout` оформляет заказ из корзины, уменьшает остатки и очищает корзину.
- История заказов и просмотр заказа по id.
- Статус заказа после checkout — `paid` (платёжный шлюз не подключён).
- Купленный датасет можно скачать по приватной ссылке Supabase Storage.
- Ссылка живёт 60 секунд; файлы датасетов хранятся в приватном bucket `datasets`.

### Медиа

- Превью товаров хранятся в Supabase PostgreSQL в таблице `media_assets`; локальной файловой системы для медиа нет.
- Файлы датасетов хранятся в приватном Supabase Storage bucket `datasets`.
- Нужен `SUPABASE_URL` и один из вариантов доступа к Storage: service role key либо publishable key с email и паролем выделенного Auth-пользователя. Если доступ не настроен, приложение падает на старте с `IllegalStateException`.
- `products.images` содержит внутренние пути `/media/assets/{uuid}`. Приложение отдаёт байты через `GET /media/assets/{id}` с `Cache-Control: public, max-age=31536000, immutable`.
- При старте приложение генерирует SVG-превью для товаров без изображений или со старыми внешними/Storage-ссылками, сохраняет их в `media_assets` и записывает в `products.images` внутренние пути. Удалённым товарам галерея очищается.
- Загрузка превью и файла датасета доступна только администратору.
- Если изображения переданы при создании или изменении товара, принимаются только существующие внутренние пути `/media/assets/{uuid}`; иначе возвращается `400`.

### Веб-интерфейс

- Страницы каталога, товара, корзины, заказов, входа, регистрации, админки, «О проекте» и страница ошибки.
- Серверный рендеринг на Thymeleaf, без отдельной сборки фронтенда.

### Документация API

- Swagger UI: `/api/docs`.
- OpenAPI JSON: `/api/docs-json` и `/api/openapi.json`.
- В спецификации описаны все основные эндпоинты, схемы запросов и ответов, bearer-авторизация.
- Коллекция Bruno `docs/bruno/DataShop` покрывает все REST-эндпоинты; инструкция — [BRUNO.md](BRUNO.md).

### Инфраструктура

- Многоступенчатый `Dockerfile`: сборка Maven и минимальный runtime-образ; процесс работает от пользователя `datashop`.
- `docker-compose.yml` с приложением и Redis 7; PostgreSQL и медиа берутся из Supabase.
- Именованный том только для Redis; данные и медиа находятся в Supabase.
- `render.yaml` для деплоя на Render.
- `.env` с реальными переменными (локальный файл, в Git не попадает); шаблон переменных приведён в README.

### Тесты

- 29 модульных тестов: JWT, Argon2id, кэш, экранирование LIKE, валидация, SVG-превью, расчёт корзины.
- Интеграционная коллекция Bruno: 37 запросов и 52 проверки, все проходят на локальном Docker-стеке.

### Git

- История оформлена по Git Flow: `main`, `develop`, feature-ветки.
- Используются Conventional Commits.
- Выпуск отмечен тегом `v1.0.0`.

## 3. Стек

| Компонент | Версия / назначение |
| --- | --- |
| Java | 21 |
| Javalin | 6.7.0 |
| Thymeleaf | 3.1.3 |
| Hibernate ORM / JPA | 6.6.5 |
| HikariCP | 6.2.1 |
| PostgreSQL | Supabase, PostgreSQL 15+ (фактически 17.11) |
| Redis | 7, опционально |
| Jedis | 5.2.0 |
| Jackson | 2.18.2 |
| JJWT | 0.12.6 |
| Argon2 | argon2-jvm 2.11 |
| Supabase Storage | REST API через `java.net.http.HttpClient` (датасеты) |
| Supabase PostgreSQL | Превью товаров в таблице `media_assets` (`bytea`) |
| OpenAPI | Javalin OpenAPI 6.7.0 (аннотации + Swagger UI) |
| Maven | 3.9+ |
| JUnit 5 / AssertJ | 5.11.4 / 3.27.3 |

## 4. Структура репозитория

```text
DataShop/
├── src/main/java/com/datashop/
│   ├── Application.java              точка входа и сборка приложения
│   ├── config/                       чтение и разбор конфигурации
│   ├── db/                           подключение к БД и Liquibase
│   ├── domain/                       JPA-сущности, enum и POJO
│   ├── repository/                   доступ к данным
│   ├── service/                      бизнес-логика, кэш, медиа, seed
│   ├── security/                     JWT, Argon2, CSRF, rate limit
│   ├── web/api/                      REST-контроллеры
│   ├── web/page/                     контроллеры Thymeleaf-страниц
│   ├── web/dto/                      DTO запросов и ответов
│   └── util/                         валидация и вспомогательные классы
├── src/main/resources/
│   ├── db/changelog/                 YAML-ченджлоги, SQL-миграции и seed-данные
│   ├── templates/                    Thymeleaf-шаблоны
│   ├── static/                       CSS и JavaScript
│   └── logback.xml
├── src/test/java/                    модульные тесты
├── Dockerfile
├── pom.xml
├── .env                              локальные переменные (не в Git)
├── docker-compose.yml
├── render.yaml
├── docs/PROJECT.md                   этот документ
└── README.md
```

## 5. Быстрый старт через Docker

### Требования

- Docker Engine 24+.
- Docker Compose v2 (`docker compose`, не `docker-compose`).
- Свободный порт 3000.

### Запуск

```bash
git clone https://github.com/balticano05/DataShop.git
cd DataShop
```

Создайте в корне проекта `.env` (он не коммитится) и задайте как минимум:

```dotenv
DATABASE_URL=postgresql://postgres.<project-ref>:<password>@aws-1-<region>.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_URL=https://<project-ref>.supabase.co
JWT_SECRET=<длинная случайная строка>
SEED_ADMIN_PASSWORD=<сильный пароль>

# Вариант 1: service role key.
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>

# Вариант 2: выделенный Supabase Auth user с RLS-политиками.
# SUPABASE_PUBLISHABLE_KEY=<publishable-key>
# SUPABASE_STORAGE_EMAIL=<storage-user-email>
# SUPABASE_STORAGE_PASSWORD=<storage-user-password>
```

`DATABASE_URL` — Session pooler URI из Supabase → Connect. Direct-хост `db.<ref>.supabase.co` работает только по IPv6 и из Docker недоступен. Без `DATABASE_URL`, `SUPABASE_URL` и доступа к Storage приложение не стартует.

Сгенерировать секрет:

```bash
openssl rand -base64 48
```

Запуск:

```bash
docker compose up --build
```

Первый запуск занимает несколько минут: собирается Maven-образ, запускается Redis, а приложение подключается к Supabase и применяет миграции.

После старта доступны:

- приложение: <http://localhost:3000>
- Swagger UI: <http://localhost:3000/api/docs>
- OpenAPI: <http://localhost:3000/api/docs-json>
- health: <http://localhost:3000/api/health>
- readiness: <http://localhost:3000/api/health/ready>

Демо-данные создаются автоматически при первом запуске:

- администратор: `admin@datashop.local` / значение `SEED_ADMIN_PASSWORD` (по умолчанию `Admin12345!`);
- промокод: `SAVE10` (10%);
- 18 демонстрационных товаров.

Пароль администратора и `JWT_SECRET` из примеров нужно заменить перед реальным использованием.

### Управление

```bash
# логи приложения
docker compose logs -f app

# состояние контейнеров
docker compose ps

# остановка
docker compose down

# остановка с удалением локального кэша Redis
docker compose down -v
```

Том `redis-data` сохраняет кэш между перезапусками. Команда `down -v` удаляет только его. Данные и медиа в Supabase не зависят от локальных томов и сохраняются.

## 6. Локальный запуск

### Требования

- JDK 21.
- Maven 3.9+.
- Проект Supabase с PostgreSQL и Storage.
- Redis не обязателен: без него используется in-memory кэш.

### Подготовка базы

Отдельная локальная база не нужна. Создайте проект Supabase, скопируйте Session pooler URI в `DATABASE_URL`, а URL проекта — в `SUPABASE_URL`. Для хранения датасетов задайте service role key либо publishable key и учётные данные выделенного Auth-пользователя. Схему, `media_assets` и seed-данные Liquibase создаст при первом запуске; превью товаров будут сгенерированы и сохранены в PostgreSQL автоматически.

### Запуск

```bash
export DATABASE_URL='postgresql://postgres.<project-ref>:<password>@aws-1-<region>.pooler.supabase.com:5432/postgres?sslmode=require'
export SUPABASE_URL='https://<project-ref>.supabase.co'
export SUPABASE_SERVICE_ROLE_KEY='<service-role-key>'
# Или без service role key:
# export SUPABASE_PUBLISHABLE_KEY='<publishable-key>'
# export SUPABASE_STORAGE_EMAIL='<storage-user-email>'
# export SUPABASE_STORAGE_PASSWORD='<storage-user-password>'
export JWT_SECRET="$(openssl rand -base64 48)"
export SEED_ADMIN_PASSWORD='StrongPassword123!'

mvn test
mvn package
java -jar target/datashop.jar
```

Приложение автоматически применит Liquibase-миграции из `db/changelog` в Supabase и выполнит seed, если он включён. `DATABASE_URL` и `SUPABASE_URL` обязательны, а для Storage нужен service role key либо publishable key с email и паролем Auth-пользователя: без этого приложение завершается на старте.

Переменные можно не экспортировать вручную, а положить в `.env` в корне проекта: приложение читает `.env` из текущей директории. Приоритет: переменные процесса → system properties → `.env`.

Если Redis не запущен, в логе появится предупреждение, и приложение продолжит работу с in-memory кэшем.

## 7. Конфигурация

### Основные параметры

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `PORT` | Порт HTTP-сервера локального запуска; Docker публикует `3000` | `3000` |
| `RENDER_EXTERNAL_URL` | Публичный URL Render; задаётся автоматически | — |
| `CORS_ORIGINS` | Разрешённые origins через запятую | `http://localhost:5173`; на Render — `RENDER_EXTERNAL_URL` |

### PostgreSQL

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `DATABASE_URL` | Session pooler URI Supabase (`postgresql://...`) | — (обязательно) |
| `DB_POOL_SIZE` | Размер пула HikariCP | `10` |

`DATABASE_URL` обязателен и должен указывать на Supabase: локальные хосты, JDBC-строки и отдельные `DB_USER`/`DB_PASSWORD` не поддерживаются. URL со схемой `postgres://` автоматически преобразуется в JDBC-URL; для Supabase нужен `sslmode=require`.

### Redis и кэш

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `REDIS_URL` | URL Redis | `redis://localhost:6379` |
| `CACHE_TTL_SECONDS` | TTL кэша каталога | `90` |

### Авторизация

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `JWT_SECRET` | Секрет подписи JWT | dev-значение, обязательно заменить |
| `JWT_EXPIRES_IN` | Время жизни токена | `1h` |
| `COOKIE_SECURE` | Передавать cookie только по HTTPS | `false` |

### Медиа

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `SUPABASE_URL` | URL проекта Supabase | — (обязательно) |
| `SUPABASE_SERVICE_ROLE_KEY` | Server-side ключ Supabase для Storage (датасеты) | — (предпочтительно) |
| `SUPABASE_PUBLISHABLE_KEY` | Публичный ключ для входа storage-пользователя | — |
| `SUPABASE_STORAGE_EMAIL` | Email выделенного Supabase Auth пользователя | — |
| `SUPABASE_STORAGE_PASSWORD` | Пароль выделенного Supabase Auth пользователя | — |
| `PREVIEW_MAX_BYTES` | Максимальный размер превью | `5242880` (5 MB) |
| `DATASET_MAX_BYTES` | Максимальный размер файла датасета | `52428800` (50 MB) |

В Supabase Storage остаётся только приватный bucket `datasets`. С service role key приложение создаёт его при первой загрузке. Без service role key нужен заранее созданный bucket и выделенный Auth-пользователь с RLS-политиками. Превью товаров в Storage не участвуют: они хранятся в `media_assets` и отдаются приложением. `Application.wire()` требует `SUPABASE_URL` и либо `SUPABASE_SERVICE_ROLE_KEY`, либо `SUPABASE_PUBLISHABLE_KEY` + `SUPABASE_STORAGE_EMAIL` + `SUPABASE_STORAGE_PASSWORD`; иначе приложение не стартует.

### Доступ к Supabase Storage без service role key

1. Supabase Dashboard → Storage: создайте `datasets` (Private). Bucket для превью не нужен.
2. Authentication → Users → Add user: задайте email и пароль, включите Auto Confirm.
3. SQL Editor: замените `<storage-user-uuid>` на UUID пользователя и выполните политики.

```sql
create policy datashop_storage_read on storage.objects
  for select to authenticated
  using (bucket_id = 'datasets'
         and auth.uid() = '<storage-user-uuid>'::uuid);

create policy datashop_storage_insert on storage.objects
  for insert to authenticated
  with check (bucket_id = 'datasets'
              and auth.uid() = '<storage-user-uuid>'::uuid);

create policy datashop_storage_update on storage.objects
  for update to authenticated
  using (bucket_id = 'datasets'
         and auth.uid() = '<storage-user-uuid>'::uuid)
  with check (bucket_id = 'datasets'
              and auth.uid() = '<storage-user-uuid>'::uuid);

create policy datashop_storage_delete on storage.objects
  for delete to authenticated
  using (bucket_id = 'datasets'
         and auth.uid() = '<storage-user-uuid>'::uuid);
```

`SupabaseMediaStorage` в этом режиме входит через `/auth/v1/token?grant_type=password`, кэширует access token и обновляет его до истечения. Пароль хранится только в `.env` на сервере. Превью в этом режиме не затрагиваются: они хранятся в PostgreSQL и не требуют Storage-политик.

### Seed-данные

| Переменная | Назначение | По умолчанию |
| --- | --- | --- |
| `SEED_ENABLED` | Создавать демо-данные при старте | `true` |
| `SEED_ADMIN_EMAIL` | Email администратора | `admin@datashop.local` |
| `SEED_ADMIN_PASSWORD` | Пароль администратора | `Admin12345!` |

Seed создаёт администратора, промокод и товары только если соответствующих данных ещё нет. Если администратор уже создан, изменение `SEED_ADMIN_PASSWORD` не меняет его пароль.

### Docker Compose

Compose не содержит PostgreSQL: `DATABASE_URL` из `.env` указывает на удалённый Supabase, а `REDIS_URL` заменяется на адрес локального сервиса `redis`.

`.env` не коммитится в репозиторий: в нём хранятся реальные секреты. Других env-файлов в проекте нет.

## 8. API и Swagger

Базовый URL локально: `http://localhost:3000`. На Render: `https://<service>.onrender.com`.

Swagger UI: `/api/docs`. OpenAPI 3.0.3: `/api/openapi.json` (алиас: `/api/docs-json`).

Спецификация описывается аннотациями `@OpenApi` на интерфейсах сервисов
(`com.datashop.service.*`), реализации находятся в пакетах `impl`.
JSON генерируется annotation processor при сборке, Swagger UI раздаётся плагином.

Авторизация:

```http
Authorization: Bearer <accessToken>
```

Токен возвращается в поле `accessToken` ответов register и login.

### 8.1. Каталог

`GET /api/products`

| Параметр | Описание | Ограничения |
| --- | --- | --- |
| `page` | Номер страницы | от 1, по умолчанию 1 |
| `limit` | Размер страницы | 1–100, по умолчанию 12 |
| `search` | Поиск по названию и описанию | до 100 символов |
| `min_price` | Минимальная цена | от 0 |
| `max_price` | Максимальная цена | от 0, не меньше `min_price` |
| `min_rating` | Минимальный рейтинг | 0–5 |
| `sort_by` | Поле сортировки | `price`, `title`, `rating`, `created_at` |
| `order` | Направление | `asc`, `desc`; по умолчанию `desc` |

По умолчанию сортировка — по дате создания, новые сверху.

Пример ответа:

```json
{
  "items": [
    {
      "id": "3f1c2a7e-9d4b-4a1e-8c2f-1b7e5d6a9c01",
      "title": "Акции Tesla 2019–2024",
      "description": "Дневные котировки TSLA с объёмами и дивидендами.",
      "price": 1499.00,
      "rating": 4.8,
      "images": [
        "/media/assets/3f1c2a7e-9d4b-4a1e-8c2f-1b7e5d6a9c01",
        "/media/assets/7a2d4b10-6e3f-4c8a-9b5d-2f1e0a3c4b02",
        "/media/assets/9c8b7a65-4d3e-4f21-8a0b-1c2d3e4f5a03"
      ],
      "stockQuantity": 25,
      "createdAt": "2026-11-08T18:00:00Z",
      "updatedAt": "2026-11-08T18:00:00Z"
    }
  ],
  "meta": {
    "total_items": 18,
    "total_pages": 2,
    "current_page": 1,
    "limit": 12
  }
}
```

Заголовок `X-Cache` показывает, был ли ответ взят из кэша.

### 8.2. Полный список эндпоинтов

| Метод | Путь | Доступ | Назначение |
| --- | --- | --- | --- |
| GET | `/api/health` | публичный | Liveness-проверка |
| GET | `/api/health/ready` | публичный | Проверка готовности и БД |
| POST | `/api/auth/register` | публичный | Регистрация покупателя |
| POST | `/api/auth/login` | публичный | Вход |
| GET | `/api/auth/me` | пользователь | Текущий пользователь |
| GET | `/api/auth/admin-check` | пользователь | Проверка роли администратора |
| POST | `/api/auth/logout` | пользователь | Выход из веб-сессии |
| GET | `/api/products` | публичный | Каталог с фильтрами |
| POST | `/api/products` | администратор | Создать товар |
| GET | `/api/products/{id}` | публичный | Товар по id |
| PATCH | `/api/products/{id}` | администратор | Изменить товар |
| DELETE | `/api/products/{id}` | администратор | Мягко удалить товар |
| GET | `/api/cart` | пользователь | Корзина |
| POST | `/api/cart/items` | пользователь | Добавить позицию |
| PATCH | `/api/cart/items/{id}` | пользователь | Изменить количество |
| DELETE | `/api/cart/items/{id}` | пользователь | Удалить позицию |
| POST | `/api/cart/apply-promo` | пользователь | Применить промокод |
| DELETE | `/api/cart/apply-promo` | пользователь | Сбросить промокод |
| POST | `/api/orders/checkout` | пользователь | Оформить заказ |
| GET | `/api/orders` | пользователь | История заказов |
| GET | `/api/orders/{id}` | пользователь | Заказ по id |
| POST | `/api/admin/media/preview` | администратор | Загрузить превью в PostgreSQL |
| GET | `/media/assets/{id}` | публичный | Выдать превью из PostgreSQL |
| POST | `/api/admin/media/dataset/{productId}` | администратор | Загрузить датасет |
| GET | `/api/downloads/{productId}` | пользователь | Приватная ссылка Supabase Storage на купленный датасет |

В веб-интерфейсе кнопка скачивания отправляет `POST /downloads/{productId}` и перенаправляет на приватную ссылку Supabase Storage. Приложение не проксирует байты датасетов через себя.

### 8.3. Формат ошибок

Ошибки API возвращаются в едином виде:

```json
{
  "statusCode": 400,
  "error": "Bad Request",
  "message": "images must contain at least 3 URLs",
  "path": "/api/products",
  "timestamp": "2026-11-09T01:00:00Z"
}
```

Коды: `400`, `401`, `403`, `404`, `405`, `409`, `413`, `429`, `500`.

## 9. Примеры запросов

### Регистрация и вход

```bash
BASE=http://localhost:3000

curl -s -X POST "$BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@example.com","password":"Password123!"}'
```

Ответ содержит `accessToken`. Сохранить токен в переменную:

```bash
TOKEN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@example.com","password":"Password123!"}' \
  | jq -r .accessToken)
```

### Вход администратора

```bash
ADMIN_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@datashop.local","password":"Admin12345!"}' \
  | jq -r .accessToken)
```

### Создание товара

```bash
curl -s -X POST "$BASE/api/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Демо-датасет",
    "description": "Пример набора данных для проверки API.",
    "price": 990.00,
    "rating": 4.5,
    "stockQuantity": 10
  }'
```

Поле `images` необязательно. Если его не передать, приложение сгенерирует три SVG-превью, сохранит их в `media_assets` и вернёт внутренние пути `/media/assets/{uuid}`. При передаче `images` принимаются только существующие внутренние пути, полученные из `POST /api/admin/media/preview`.

### Каталог с фильтрами

```bash
curl -s "$BASE/api/products?page=1&limit=12&search=логи&min_price=500&max_price=3000&min_rating=4&sort_by=price&order=asc" | jq
```

Проверка кэша:

```bash
curl -sI "$BASE/api/products?page=1&limit=12" | grep -i x-cache
```

### Корзина и промокод

```bash
PRODUCT_ID=<uuid-товара>

curl -s -X POST "$BASE/api/cart/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":1}"

curl -s -X POST "$BASE/api/cart/apply-promo" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"code":"SAVE10"}'

curl -s "$BASE/api/cart" -H "Authorization: Bearer $TOKEN" | jq
```

### Checkout

```bash
curl -s -X POST "$BASE/api/orders/checkout" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Ссылка на скачивание

```bash
curl -s "$BASE/api/downloads/$PRODUCT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Скачивание доступно только после покупки товара.

## 10. Тесты

```bash
mvn test
```

Покрыты:

- выпуск, разбор и проверка JWT, включая истёкшие и повреждённые токены;
- хеширование и проверка паролей Argon2id;
- попадание и инвалидация кэша, TTL;
- стабильность ключей кэша для одинаковых запросов;
- экранирование wildcard-символов в SQL LIKE;
- валидация email, пароля, изображений и других полей;
- расчёт суммы, скидки и итога корзины.

Все 29 тестов проходят. Интеграционные проверки с реальными Supabase PostgreSQL, Supabase Storage, Redis и Render выполняются вручную.

REST API дополнительно проверяется коллекцией Bruno из `docs/bruno/DataShop`: 37 запросов и 52 проверки, покрыты все эндпоинты, RBAC, фильтры, кэш, корзина, промокод, checkout, превью в PostgreSQL, Supabase Storage и скачивание. Инструкция — [BRUNO.md](BRUNO.md).

## 11. Docker и инфраструктура

### Dockerfile

- Первый stage: `maven:3.9.9-eclipse-temurin-21-alpine`, сборка `datashop.jar`.
- Второй stage: `eclipse-temurin:21-jre-jammy` с готовым jar.
- Процесс запускается от непривилегированного пользователя `datashop`.
- Healthcheck приложения задаётся в `docker-compose.yml`.
- Порт по умолчанию — 3000.

### Docker Compose

`docker compose up --build` поднимает два сервиса:

| Сервис | Образ | Назначение |
| --- | --- | --- |
| `app` | сборка из `Dockerfile` | приложение |
| `redis` | `redis:7-alpine` | кэш каталога |

- PostgreSQL не поднимается локально: приложение подключается к удалённому Supabase по `DATABASE_URL`.
- У обоих сервисов настроены healthcheck и `restart: unless-stopped`.
- Приложение ждёт готовности Redis.
- Данные каталога, пользователей и заказов хранятся в Supabase PostgreSQL; локальный том есть только у Redis.
- Превью хранятся в Supabase PostgreSQL (`media_assets`), датасеты — в Supabase Storage; временные multipart-файлы Javalin пишутся в системный `/tmp`.
- `DATABASE_URL`, `SUPABASE_URL` и доступ к Storage (service role key либо publishable key с учётными данными Auth-пользователя) обязательны при запуске compose.
- Секреты передаются через `.env`, который не попадает в Git.
- Compose передаёт `.env` в контейнер через `env_file` и заменяет только `REDIS_URL` на адрес сервиса `redis`.

Проверка конфигурации без запуска:

```bash
docker compose config
```

## 12. Деплой на Render

### 12.1. Что подготовить

1. Публичный GitHub-репозиторий с проектом.
2. Проект Supabase:
   - создать проект и базу PostgreSQL;
   - взять Session pooler connection string и добавить `sslmode=require`;
   - скопировать `SUPABASE_URL` и один из вариантов доступа к Storage: `SUPABASE_SERVICE_ROLE_KEY` либо `SUPABASE_PUBLISHABLE_KEY` + `SUPABASE_STORAGE_EMAIL` + `SUPABASE_STORAGE_PASSWORD`;
   - с service role key приватный bucket `datasets` создаётся автоматически; без него создайте его заранее и настройте RLS. Bucket для превью не нужен: они хранятся в PostgreSQL.
3. Аккаунт Render.

### 12.2. Деплой через Blueprint

Открыть ссылку:

<https://render.com/deploy?repo=https://github.com/balticano05/DataShop>

Render прочитает `render.yaml` и создаст:

- web-сервис `datashop` из `Dockerfile`;
- Render Key Value `datashop-redis`;
- переменные окружения.

При создании нужно заполнить секреты:

| Переменная | Значение |
| --- | --- |
| `DATABASE_URL` | Session pooler URL Supabase с `sslmode=require` |
| `SUPABASE_URL` | URL проекта Supabase |
| `SUPABASE_SERVICE_ROLE_KEY` | service role key Supabase (предпочтительно) |
| `SUPABASE_PUBLISHABLE_KEY` | publishable key, если используется Auth-пользователь |
| `SUPABASE_STORAGE_EMAIL` | email выделенного Supabase Auth пользователя |
| `SUPABASE_STORAGE_PASSWORD` | пароль выделенного Supabase Auth пользователя |
| `SEED_ADMIN_PASSWORD` | сильный пароль администратора |

`DATABASE_URL` и `SUPABASE_URL` обязательны. Для Storage заполните либо `SUPABASE_SERVICE_ROLE_KEY`, либо три переменные Auth-пользователя. Без доступа к Storage контейнер завершится на старте. `JWT_SECRET` Render генерирует автоматически. `REDIS_URL` подставляется из Key Value.

Дополнительно можно задать:

| Переменная | Значение |
| --- | --- |
| `CORS_ORIGINS` | URL фронтенда прошлого семестра; на Render по умолчанию используется `RENDER_EXTERNAL_URL` |
| `SEED_ENABLED` | `true` для демо-данных |
| `COOKIE_SECURE` | `true` на HTTPS |
| `DB_POOL_SIZE` | `5` для free-плана |

После создания сервиса Render выполнит сборку и запуск. Сборка занимает несколько минут.

### 12.3. Ручной вариант

Если Blueprint не используется:

1. Создать Web Service → Docker.
2. Подключить репозиторий, выбрать ветку `main`.
3. Указать:
   - Dockerfile Path: `Dockerfile`;
   - Docker Context: `.`;
   - Health Check Path: `/api/health`.
4. Создать Render Key Value и скопировать внутренний connection string в `REDIS_URL`.
5. Добавить переменные из таблицы выше.
6. Запустить деплой.

### 12.4. Проверка после деплоя

```bash
SERVICE=https://<service>.onrender.com

curl -fsS "$SERVICE/api/health"
curl -fsS "$SERVICE/api/health/ready"
curl -fsS "$SERVICE/api/docs-json" | jq '.info'
```

Затем открыть `$SERVICE/api/docs` и проверить:

1. Вход администратора.
2. Создание товара с тремя изображениями.
3. Фильтры и сортировку каталога.
4. Заголовок `X-Cache`.
5. Удаление товара и инвалидацию кэша.
6. Добавление товара в корзину.
7. Применение `SAVE10`.
8. Checkout и скачивание датасета.
9. Загрузку превью и файла датасета.

### 12.5. Ограничения free-плана Render

- 0.1 CPU и 512 MB RAM.
- Сервис засыпает после 15 минут без запросов; первый запрос после сна может выполняться дольше.
- Нет постоянного диска, но он не нужен: временные multipart-файлы пишутся в `/tmp` и очищаются при рестарте.
- Превью хранятся в Supabase PostgreSQL, датасеты — в Supabase Storage, поэтому рестарт и передеплой не влияют на загруженные файлы.
- Render Key Value на free-плане подходит только для кэша; данные каталога хранятся в Supabase PostgreSQL.

Для учебного демо этих ограничений достаточно. Постоянное хранение данных и медиа обеспечивает Supabase.

## 13. Git Flow

В репозитории используются ветки:

- `main` — стабильные релизы;
- `develop` — интеграция изменений;
- `feature/*` — отдельные задачи;
- `release/*` — подготовка релиза (при необходимости);
- `hotfix/*` — срочные исправления (при необходимости).

Текущий релиз отмечен тегом `v1.0.0`.

Новая задача:

```bash
git switch develop
git pull
git switch -c feature/my-change

# изменения
git add .
git commit -m "feat: add my change"
git push -u origin feature/my-change
```

После проверки ветка вливается в `develop` через merge с `--no-ff` (или pull request).

Релиз:

```bash
git switch -c release/1.1.0 develop
# проверки и исправления
git switch main
git merge --no-ff release/1.1.0
git tag -a v1.1.0 -m "DataShop 1.1.0"
git switch develop
git merge --no-ff main
git push origin main develop --tags
```

Формат сообщений — Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `build:`, `refactor:`, `chore:`.

## 14. Безопасность

- Пароли хранятся только в виде Argon2id-хеша.
- JWT подписывается секретом `JWT_SECRET`; срок жизни по умолчанию 1 час.
- Для API используется `Authorization: Bearer`, для веб-интерфейса — HttpOnly cookie.
- Формы веб-интерфейса защищены CSRF-токенами.
- Мутации каталога и загрузка медиа требуют роль администратора.
- Регистрация, вход и checkout ограничены по частоте.
- Добавлены security-заголовки.
- Превью товаров хранятся в Supabase PostgreSQL и отдаются приложением по внутренним путям `/media/assets/{id}`.
- Файлы датасетов хранятся в приватном bucket Supabase Storage и отдаются по временным подписанным ссылкам (60 секунд).
- Приложение не хранит медиа локально; без `DATABASE_URL` и настроенного доступа к Supabase оно не стартует.
- CORS настраивается через `CORS_ORIGINS`; credentials разрешены только для указанных origins.
- `.env` с реальными секретами исключён из Git; других env-файлов в репозитории нет.

## 15. Ограничения и риски

1. **Стек отличается от NestJS + TypeORM.** Backend реализован на Java 21, Javalin и Hibernate/JPA. По условиям задания другой язык допускается только по согласованию с преподавателем, а TypeORM к Java неприменим. Это главный риск приёмки.
2. **Фактический деплой не выполнялся.** Проверены `render.yaml`, Dockerfile, `docker compose config`, локальная сборка и тесты. End-to-end проверка на Render и Supabase остаётся ручным шагом.
3. **База и медиа полностью зависят от Supabase.** Без `DATABASE_URL`, `SUPABASE_URL` и настроенного доступа к Storage приложение не стартует; при недоступности Supabase каталог, превью, загрузка файлов и выдача ссылок завершаются ошибкой.
4. **Seed администратора выполняется один раз.** Если администратор уже создан, смена `SEED_ADMIN_PASSWORD` не обновляет пароль.
5. **Checkout не использует платёжный шлюз.** Заказ сразу получает статус `paid`.
6. **Нет refresh-токенов, подтверждения email и восстановления пароля.** После истечения JWT нужно войти заново.
7. **Нет CI.** Тесты и сборка запускаются локально или вручную.
8. **In-memory fallback не разделяется между инстансами.** При нескольких экземплярах приложения нужен Redis.
9. **`GET /api/health` не проверяет БД.** Для проверки готовности базы используется `/api/health/ready`.
10. **Файлы демо-датасетов из Neon не перенесены.** В Neon хранились только идентификаторы вида `datashop/datasets/demo-*`, а не сами файлы. После перехода на Supabase Storage такие товары возвращают `404 Dataset file is not available`, пока администратор не загрузит датасет заново.

## 16. Чек-лист перед сдачей

- [ ] Стек Java/Javalin согласован с преподавателем.
- [ ] В `.env` заменены `JWT_SECRET`, `SEED_ADMIN_PASSWORD`, пароль PostgreSQL Supabase и доступ к Storage (service role key или Auth-пользователь).
- [ ] `mvn test` — все 29 тестов проходят.
- [ ] `mvn package` — jar собирается.
- [ ] `cd docs/bruno/DataShop && bru run --env Local` — 37/37 запросов и 52/52 проверки.
- [ ] `DATABASE_URL`, `SUPABASE_URL` и доступ к Storage заданы; без них приложение не стартует.
- [ ] `docker compose config` — ошибок нет.
- [ ] `docker compose up --build` — приложение и Redis стартуют, PostgreSQL не поднимается локально.
- [ ] Открываются `/`, `/api/docs`, `/api/health`, `/api/health/ready`.
- [ ] Работает вход администратора.
- [ ] Создаются товар с тремя изображениями и файл датасета.
- [ ] Работают фильтры, сортировка и пагинация каталога.
- [ ] `X-Cache` меняется с `MISS` на `HIT`, после изменения товара снова `MISS`.
- [ ] Работают корзина, `SAVE10`, checkout и скачивание.
- [ ] На Render заданы `DATABASE_URL`, `SUPABASE_URL`, доступ к Storage и `SEED_ADMIN_PASSWORD`.
- [ ] При необходимости в `CORS_ORIGINS` добавлен URL фронтенда прошлого семестра.
- [ ] Проверены `/api/health` и `/api/docs` на Render.
- [ ] Выполнен тестовый сценарий покупки и скачивания на Render.
