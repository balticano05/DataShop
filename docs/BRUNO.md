# Проверка REST API через Bruno

В репозитории лежит готовая коллекция Bruno:

```text
docs/bruno/DataShop/
```

Она покрывает все REST-эндпоинты приложения: здоровье и документацию, аутентификацию, каталог, корзину, промокод, заказы, загрузку превью в PostgreSQL, загрузку датасета в Supabase Storage, выдачу превью, скачивание датасета и негативные сценарии RBAC.

## Требования

- Приложение запущено на `http://localhost:3000`.
- Выполнен seed (`SEED_ENABLED=true`), поэтому существует администратор `admin@datashop.local`.
- В `.env` задан рабочий доступ к Supabase: `DATABASE_URL`, `SUPABASE_URL` и `SUPABASE_SERVICE_ROLE_KEY` либо `SUPABASE_PUBLISHABLE_KEY` + `SUPABASE_STORAGE_EMAIL` + `SUPABASE_STORAGE_PASSWORD`. Запрос `Admin Media/Upload Preview` пишет в PostgreSQL, `Upload Dataset` и `Downloads/Get Download Link` обращаются к Supabase Storage.
- Пароль администратора в коллекции совпадает с `SEED_ADMIN_PASSWORD` из `.env`. По умолчанию в `Local.bru` указан `Admin12345!`.

## Запуск

### В приложении Bruno

1. Откройте Bruno.
2. Нажмите **Open Collection** и выберите папку `docs/bruno/DataShop`.
3. В правом верхнем углу выберите окружение **Local**.
4. Запустите коллекцию целиком: **Run** → **Run Collection**.

Запускать запросы по одному тоже можно, но коллекция stateful: логин сохраняет токены, создание товара сохраняет `productId`, оформление заказа сохраняет `orderId`. При одиночном запуске сначала выполните запросы из папок `Auth` и `Products`.

### Из терминала

```bash
cd docs/bruno/DataShop
npx @usebruno/cli run --env Local
```

Или с глобально установленным CLI:

```bash
npm install -g @usebruno/cli
cd docs/bruno/DataShop
bru run --env Local
```

Ожидаемый результат на чистом окружении:

```text
Requests: 37 (37 Passed)
Tests:    52/52
```

## Переменные окружения коллекции

Файл: `docs/bruno/DataShop/environments/Local.bru`.

| Переменная | Назначение |
| --- | --- |
| `baseUrl` | Адрес API, по умолчанию `http://localhost:3000` |
| `adminEmail` | Email администратора из seed |
| `adminPassword` | Пароль администратора из `SEED_ADMIN_PASSWORD` |
| `customerEmail` | Email тестового покупателя |
| `customerPassword` | Пароль тестового покупателя |

Во время выполнения коллекция сохраняет в runtime-переменные:

- `adminToken`, `customerToken` — JWT после логина;
- `productId` — товар из каталога или созданный администратором;
- `deleteProductId` — товар для проверки RBAC и удаления;
- `cartItemId` — позиция корзины;
- `orderId` — оформленный заказ;
- `previewUrl` — внутренний путь `/media/assets/{uuid}` загруженного превью;
- `datasetPublicId` — путь файла, загруженного в Supabase Storage.

Токены и пароли не коммитятся в виде секретов приложения: в коллекции только демонстрационные данные локального seed-пользователя.

## Порядок папок

Коллекция запускается в порядке `seq` папок. Этот порядок важен, потому что запросы передают данные друг другу.

1. **Health & Docs** — health/readiness, Swagger UI, OpenAPI, CORS preflight.
2. **Auth** — регистрация покупателя, логин администратора и покупателя, `me`, `admin-check`, logout.
3. **Products** — каталог, фильтры/сортировка/пагинация, карточка товара, создание и изменение товара администратором.
4. **Cart** — чтение корзины, добавление, изменение и удаление позиции, повторное добавление, применение `SAVE10`.
5. **Orders** — checkout, список заказов, заказ по ID.
6. **Admin Media** — upload превью в PostgreSQL, выдача превью по `/media/assets/{id}` и upload датасета в Supabase Storage; датасет привязывается к созданному товару.
7. **Downloads** — временная приватная ссылка Supabase Storage после оплаченного заказа.
8. **Negative** — 401 без токена, 403 для customer, 400 при неверном `limit`, 404 для неизвестного промокода.
9. **Cleanup** — снятие промокода и удаление тестового товара администратором.

## Покрытие эндпоинтов

| Метод | Путь | Папка | Доступ |
| --- | --- | --- | --- |
| `GET` | `/api/health` | Health & Docs | публичный |
| `GET` | `/api/health/ready` | Health & Docs | публичный |
| `GET` | `/api/docs` | Health & Docs | публичный |
| `GET` | `/api/openapi.json` | Health & Docs | публичный |
| `OPTIONS` | `/api/products` | Health & Docs | CORS preflight |
| `POST` | `/api/auth/register` | Auth | публичный |
| `POST` | `/api/auth/login` | Auth | публичный |
| `GET` | `/api/auth/me` | Auth | JWT |
| `GET` | `/api/auth/admin-check` | Auth | admin |
| `POST` | `/api/auth/logout` | Auth | JWT |
| `GET` | `/api/products` | Products | публичный |
| `GET` | `/api/products/{id}` | Products | публичный |
| `POST` | `/api/products` | Products | admin |
| `PATCH` | `/api/products/{id}` | Products | admin |
| `DELETE` | `/api/products/{id}` | Negative, Cleanup | admin |
| `GET` | `/api/cart` | Cart | JWT |
| `POST` | `/api/cart/items` | Cart | JWT |
| `PATCH` | `/api/cart/items/{id}` | Cart | JWT |
| `DELETE` | `/api/cart/items/{id}` | Cart | JWT |
| `POST` | `/api/cart/apply-promo` | Cart | JWT |
| `DELETE` | `/api/cart/apply-promo` | Cleanup | JWT |
| `POST` | `/api/orders/checkout` | Orders | JWT |
| `GET` | `/api/orders` | Orders | JWT |
| `GET` | `/api/orders/{id}` | Orders | JWT |
| `POST` | `/api/admin/media/preview` | Admin Media | admin |
| `GET` | `/media/assets/{id}` | Admin Media | публичный |
| `POST` | `/api/admin/media/dataset/{productId}` | Admin Media | admin |
| `GET` | `/api/downloads/{productId}` | Downloads | JWT + оплаченный заказ |

## Проверяемые требования

- JWT и RBAC: customer не может создавать/удалять товары, запросы без токена получают `401`.
- Каталог: `page`, `limit`, `search`, `min_price`, `max_price`, `min_rating`, `sort_by`, `order`; ответ содержит `items` и `meta`.
- Валидация: `limit=1000` возвращает `400`.
- Кэш: ответы каталога содержат заголовок `X-Cache` (`HIT`/`MISS`); изменение каталога инвалидирует кэш.
- Корзина и промокод: `SAVE10` даёт скидку 10%, итог меньше подытога.
- Заказ: checkout возвращает статус `paid`.
- Превью: загружаются в Supabase PostgreSQL как `media_assets`, URL имеет вид `/media/assets/{uuid}` и отдаётся с заголовком `immutable`.
- Supabase Storage: датасет загружается в облако и получает путь с префиксом `datasets/`.
- Скачивание: после оплаты возвращается временная подписанная ссылка Supabase Storage.
- CORS: preflight с `Origin` разрешённого фронтенда возвращает `Access-Control-Allow-Origin`.

## Если запрос не проходит

- `401` в защищённых запросах — сначала выполните `Auth/Login Admin` и `Auth/Login Customer`.
- `409` при повторной регистрации — это нормально; коллекция продолжает работу через логин.
- `403` у административных запросов — проверьте `adminEmail`/`adminPassword` и seed.
- `404` у промокода — проверьте, что seed-миграция выполнилась и промокод `SAVE10` активен.
- `500` в `Admin Media/Upload Preview` — проверьте `DATABASE_URL` и доступность Supabase PostgreSQL.
- `500` в `Admin Media/Upload Dataset` — проверьте `SUPABASE_URL`, доступ к Storage (service role key или Auth-пользователь) и доступность Supabase Storage.
- `500` в `Downloads/Get Download Link` — сначала выполните `Orders/Checkout`, затем `Admin Media/Upload Dataset`: товар должен иметь файл, а покупатель — оплаченный заказ.
- `400` в `Negative/Products Invalid Limit` — ожидаемый результат, тест проходит.

## Запуск против задеплоенного API

Создайте копию окружения, например `Production.bru`, и замените `baseUrl`:

```text
vars {
  baseUrl: https://<service>.onrender.com
  adminEmail: admin@datashop.local
  adminPassword: <SEED_ADMIN_PASSWORD>
  customerEmail: bruno.customer@example.com
  customerPassword: Password123
}
```

Для публичного API должны быть настроены `CORS_ORIGINS`, `SUPABASE_URL` и доступ к Storage. Если используется удалённая база Supabase, администратор должен существовать в ней после первого запуска с `SEED_ENABLED=true`.
