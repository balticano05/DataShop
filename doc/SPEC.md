# DataShop — Полный план проекта (Вариант 2: NestJS + PostgreSQL)

Разбит на **150 задач**, сгруппированных по эпикам. Каждая задача — конкретное действие с понятным результатом. Охвачены: подготовка, backend, инфраструктура, тесты, Docker, деплой и фронтенд.

---

## Эпик 1. Подготовка репозитория и окружения (1–5)

1. [x] **Создать git-репозиторий `datashop`** — инициализировать, подключить remote, сделать первый коммит `chore: init repository`.
2. [x] **Создать ветки `main` и `develop`** — `main` только для релизов, вся разработка через `develop`.
3. [x] **Добавить `.gitignore`, `.editorconfig`, `.env.example`** — исключить `node_modules`, `dist`, `.env`; шаблон переменных окружения в репозитории.
4. **Настроить ESLint, Prettier, strict TypeScript, Conventional Commits** — единый стиль, шаблон PR, обязательные префиксы `feat:`, `fix:`, `chore:`, `refactor:`.
5. **Завести аккаунты Neon, Cloudinary, Render, Redis (Render KV / Upstash)** — установить Node.js LTS, Nest CLI, Docker Desktop, собрать все credentials в локальный `.env`.

---

## Эпик 2. Каркас NestJS (6–15)

6. **Создать NestJS-проект `backend`** — `nest new backend`, удалить лишние модули.
7. **Подключить `@nestjs/config` и `joi`** — конфиг через `ConfigModule.forRoot({ isGlobal: true })`.
8. **Создать `configuration.ts` и `env.validation.ts`** — типизированный доступ к env и Joi-схема с обязательными переменными.
9. **Настроить `main.ts`** — префикс `/api`, `helmet`, `shutdown hooks`, `x-powered-by` отключён.
10. **Включить глобальный `ValidationPipe`** — `whitelist`, `forbidNonWhitelisted`, `transform`.
11. **Создать `AllExceptionsFilter`** — единый формат ошибок `{ statusCode, error, message, path, timestamp }`.
12. **Подключить `nestjs-pino`** — структурные логи, без паролей и токенов.
13. **Настроить CORS по whitelist** — `CORS_ORIGINS` из env, без `*`.
14. **Создать `HealthModule`** — `/api/health` с проверкой БД и Redis.
15. **Проверить запуск приложения** — `GET /api/health` возвращает 200, при отсутствии обязательной переменной приложение не стартует.

---

## Эпик 3. База данных и TypeORM (16–30)

16. **Подключить TypeORM через `ConfigService`** — `forRootAsync`, `synchronize: false`.
17. **Создать `data-source.ts` для CLI** — отдельный DataSource для миграций.
18. **Настроить скрипты `migration:generate/run/revert/seed`** — в `package.json`.
19. **Описать сущность `User`** — `id`, `email`, `password_hash`, `role`, `created_at`.
20. **Описать сущность `Product`** — включая `images[]`, `stock_quantity`, `file_public_id`, `deleted_at`.
21. **Описать сущности `Cart` и `CartItem`** — 1:1 с User, составной unique `(cart_id, product_id)`.
22. **Описать сущность `PromoCode`** — `code` unique upper-case, `discount_percentage`, `is_active`.
23. **Описать сущности `Order` и `OrderItem`** — enum статуса, snapshot цены и названия.
24. **Добавить transformer `numeric → number`** — чтобы цена приходила числом, а не строкой.
25. **Добавить `@DeleteDateColumn` для `Product`** — soft-delete.
26. **Добавить индексы** — `products(price)`, `products(rating)`, `products(title)`.
27. **Добавить unique-ограничения** — email, promo code, cart user.
28. **Добавить `CHECK (stock_quantity >= 0)`** — защита от отрицательного остатка.
29. **Сгенерировать первую миграцию** — `migration:generate`, проверить `run` и `revert`.
30. **Написать seed** — админ, `SAVE10`, 15–20 демо-товаров с 3 превью каждый.

---

## Эпик 4. Аутентификация и RBAC (31–45)

31. **Создать `UsersModule` и `UsersService`** — поиск по email, создание пользователя.
32. **Реализовать `AuthModule`** — `register`, `login`, `me`.
33. **Подключить `argon2id`** — хеширование паролей.
34. **Валидировать DTO регистрации и входа** — email, пароль ≥ 8 символов.
35. **Запретить передачу `role` при регистрации** — роль всегда `customer`.
36. **Создавать пустую корзину при регистрации** — в одной транзакции с созданием пользователя.
37. **Создать `JwtStrategy`** — payload `sub`, `role`, срок 1 час.
38. **Включить глобальный `JwtAuthGuard`** — доступ закрыт по умолчанию.
39. **Создать декоратор `@Public()`** — для открытых маршрутов.
40. **Создать `RolesGuard` и `@Roles()`** — ролевой доступ.
41. **Создать `@CurrentUser()`** — удобное извлечение пользователя из запроса.
42. **Сделать одинаковые ошибки входа** — не раскрывать, что неверно: email или пароль.
43. **Поставить rate limit на auth** — 5 req/min на `/auth/login` и `/auth/register`.
44. **Поставить глобальный rate limit** — 100 req/min через `ThrottlerModule`.
45. **Проверить RBAC** — customer получает 403 на админ-маршрутах, без токена — 401.

---

## Эпик 5. Каталог товаров (46–65)

46. **Создать `ProductsModule`** — controller, service, repository.
47. **Создать `QueryProductsDto`** — все query-параметры с валидацией.
48. **Ввести enum для `sort_by` и `order`** — whitelist значений.
49. **Реализовать `GET /api/products`** — QueryBuilder с фильтрами.
50. **Реализовать поиск `ILIKE` по title и description** — без учёта регистра.
51. **Реализовать `escapeLike`** — экранирование `%`, `_`, `\`.
52. **Реализовать фильтры `min_price`, `max_price`** — с проверкой `min <= max`.
53. **Реализовать фильтр `min_rating`** — 0–5.
54. **Реализовать сортировку** — `price`/`title` + `asc`/`desc` + `addOrderBy('id')` для стабильности.
55. **Реализовать пагинацию** — `skip`/`take` + `getManyAndCount`.
56. **Сформировать формат ответа `{ items, meta }`** — как в требованиях.
57. **Реализовать `GET /api/products/:id`** — без приватных полей.
58. **Создать response-маппер `ProductDto`** — исключить `file_public_id`.
59. **Реализовать `POST /api/products`** — только admin.
60. **Реализовать `PATCH /api/products/:id`** — только admin.
61. **Реализовать `DELETE /api/products/:id`** — soft-delete + инвалидация кэша.
62. **Валидировать DTO товара** — минимум 3 изображения, цена > 0, stock ≥ 0.
63. **Проверить, что удалённый товар не попадает в каталог** — `deleted_at IS NULL` автоматически.
64. **Включить SQL-логи только в dev** — `logging: ['query']` под флагом.
65. **Проверить `sort_by=hack` → 400** — защита от инъекций в сортировку.

---

## Эпик 6. Кэширование каталога (66–75)

66. **Создать `CacheModule` на `ioredis`** — провайдер подключения к Redis.
67. **Реализовать `CatalogCacheService`** — методы `get`, `set`, `invalidateAll`.
68. **Строить ключ `catalog:sha1(normalized query)`** — параметры отсортированы по имени.
69. **Настроить TTL 90 секунд** — из `CACHE_TTL_SECONDS`.
70. **Возвращать `X-Cache: HIT|MISS`** — через `CacheHeaderInterceptor`.
71. **Инвалидировать кэш при `POST/PATCH/DELETE /products`** — `SCAN` + `UNLINK` по маске `catalog:*`.
72. **Инвалидировать кэш после checkout** — меняется остаток.
73. **Обеспечить работу API без Redis** — try/catch, лог, продолжение работы.
74. **Проверить HIT/MISS** — второй одинаковый запрос возвращает HIT.
75. **Проверить инвалидацию** — после DELETE следующий запрос возвращает MISS.

---

## Эпик 7. Корзина и промокод (76–90)

76. **Создать `CartModule`** — controller, service.
77. **Реализовать `GET /api/cart`** — позиции, subtotal, discount, total.
78. **Реализовать `POST /api/cart/items`** — добавление, при повторе количество суммируется.
79. **Реализовать `PATCH /api/cart/items/:id`** — изменение количества 1…остаток.
80. **Реализовать `DELETE /api/cart/items/:id`** — удаление позиции.
81. **Проверять остаток и существование товара** — 404/409 при проблемах.
82. **Ограничить доступ к корзине `user.id`** — чужая корзина → 404.
83. **Создать `PromoModule` и `PromoService`** — поиск и валидация кода.
84. **Реализовать `POST /api/cart/apply-promo`** — привязка кода к корзине.
85. **Проверять `is_active` промокода** — неактивный → понятная ошибка.
86. **Искать код без учёта регистра** — `SAVE10` = `save10`.
87. **Вынести расчёт `subtotal/discount/total` в чистую функцию** — используется в корзине и checkout.
88. **Проверить скидку ровно −10%** — unit-тест.
89. **Проверить уникальность `(cart_id, product_id)`** — составной индекс.
90. **Обработать пустую корзину при checkout** — 400 с понятным сообщением.

---

## Эпик 8. Заказы и checkout (91–100)

91. **Создать `OrdersModule`** — controller, service.
92. **Реализовать транзакционный `POST /api/orders/checkout`** — `dataSource.transaction`.
93. **Атомарное списание остатков** — `UPDATE products SET stock_quantity = stock_quantity - :q WHERE id = :id AND stock_quantity >= :q`.
94. **Возвращать 409 при нехватке остатка** — 0 затронутых строк → rollback.
95. **Брать цены из БД, а не из запроса** — защита от подмены цены.
96. **Создавать `Order` и `OrderItem` со snapshot** — `unit_price`, `title_snapshot`.
97. **Очищать корзину и промокод после успешного заказа** — в той же транзакции.
98. **Реализовать `GET /api/orders`** — только свои заказы.
99. **Реализовать `GET /api/orders/:id`** — только свой, иначе 404.
100. **Создать `PaymentsService`-заглушку** — интерфейс под будущий провайдер, сейчас всегда успех.

---

## Эпик 9. Медиа и защищённое скачивание (101–110)

101. **Создать `MediaModule`** — провайдер Cloudinary.
102. **Реализовать `POST /api/admin/media/preview`** — публичная загрузка превью.
103. **Реализовать `POST /api/admin/media/dataset/:productId`** — загрузка файла базы.
104. **Ограничить размер и MIME-тип загрузки** — через `FileInterceptor`.
105. **Сохранять только `file_public_id`** — файл не отдаётся напрямую.
106. **Создать `DownloadsModule` и `DownloadsService`** — проверка покупки.
107. **Реализовать `GET /api/downloads/:productId`** — 403, если не куплено.
108. **Генерировать signed URL на 60 секунд** — через Cloudinary SDK.
109. **Логировать выдачу ссылок** — user, product, timestamp.
110. **Проверить, что прямая ссылка не работает** — вручную открыть `file_public_id` → 401.

---

## Эпик 10. Swagger и документация API (111–120)

111. **Подключить `@nestjs/swagger`** — `/api/docs`.
112. **Добавить `@ApiTags`** — по модулям.
113. **Добавить `@ApiBearerAuth`** — на защищённые маршруты.
114. **Добавить `@ApiProperty` в DTO** — примеры значений.
115. **Добавить примеры ответов** — через `@ApiResponse`.
116. **Настроить кнопку Authorize** — ввод Bearer-токена.
117. **Описать формат ошибок в Swagger** — единая схема.
118. **Добавить описание query-параметров** — правила и значения по умолчанию.
119. **Проверить все эндпоинты в Swagger UI** — вручную прогнать.
120. **Связать Swagger с README** — ссылка на публичный URL.

---

## Эпик 11. Тесты (121–130)

121. **Настроить Jest** — `jest.config.ts`, coverage.
122. **Unit: расчёт скидки** — 10% от subtotal.
123. **Unit: `escapeLike`** — экранирование `%`, `_`, `\`.
124. **Unit: ключ кэша** — одинаковые параметры в разном порядке → один ключ.
125. **Unit: `CatalogCacheService`** — мок ioredis.
126. **E2E: auth** — регистрация, вход, `/me`, 401 без токена.
127. **E2E: RBAC** — customer → 403 на админ-маршрутах.
128. **E2E: каталог** — фильтры, сортировка, пагинация, `meta`.
129. **E2E: корзина и промокод** — добавление, `SAVE10`, −10%.
130. **E2E: checkout и download** — покупка, ссылка, истечение через минуту.

---

## Эпик 12. Docker и локальный запуск (131–138)

131. **Multi-stage Dockerfile** — стадии `deps`, `build`, `prod-deps`, `runtime`.
132. **Runtime под `USER node`** — непривилегированный пользователь.
133. **Команда запуска** — `node dist/database/run-migrations.js && node dist/main.js`.
134. **`docker-compose.yml`** — `app`, `postgres`, `redis`.
135. **Именованные volumes** — `pgdata`, `redisdata`.
136. **Healthcheck** — `pg_isready`, `redis-cli ping`, `depends_on: service_healthy`.
137. **`.env.example` с полным списком переменных** — включая Cloudinary и JWT.
138. **Проверка `docker compose up --build`** — всё поднимается одной командой, данные сохраняются.

---

## Эпик 13. Деплой в облако (139–144)

139. **Neon** — создать проект, скопировать connection string с `sslmode=require`.
140. **Миграции и seed на Neon** — прогнать с локальной машины или при старте контейнера.
141. **Cloudinary** — создать аккаунт, загрузить демо-превью и файлы баз как `authenticated raw`.
142. **Redis в облаке** — Render KV или Upstash, `REDIS_URL` в env.
143. **Render Web Service из Dockerfile** — health check path `/api/health`.
144. **CORS и переменные окружения на Render** — `CORS_ORIGINS` с боевым доменом фронтенда.

---

## Эпик 14. Фронтенд (145–150+)

145. **Инициализировать `frontend` на Vite + React + React Router** — структура: `api/`, `context/`, `components/`, `pages/`, `routes/`.
146. **Реализовать `api/client.js`** — fetch-обёртка: base URL, Bearer, обработка 401/403/429.
147. **Реализовать `AuthContext` и `CartContext`** — токен в `sessionStorage`, состояние корзины.
148. **Реализовать каталог `/`** — поиск с debounce 400 мс, фильтры, сортировка, пагинация, параметры в URL.
149. **Реализовать карточку товара `/product/:id`** — слайдер ≥ 3 превью, выпадающее описание, кнопка «В корзину».
150. **Реализовать `/cart`, `/login`, `/register`, `/orders`, `/admin`** — корзина с промокодом, «Мои покупки» с кнопкой «Скачать», админка с CRUD и загрузкой файлов; `ProtectedRoute` и `AdminRoute` для ограничения доступа.

---

## Итог

- **Всего задач:** 150.
- **Backend:** эпики 2–13 (пункты 6–144).
- **Frontend:** эпик 14 (пункты 145–150).
- **Подготовка:** эпик 1 (пункты 1–5).
- **Порядок:** можно двигаться строго по номерам; фронтенд (145–150) можно начинать параллельно после эпика 5, подключая новые разделы по мере готовности API.