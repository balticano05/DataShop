# DataShop

Маркетплейс датасетов: каталог, корзина и промокоды, заказы, загрузка медиа и
защищённое скачивание купленных файлов.

## API-документация (Swagger)

Интерактивная документация: **`/api/docs`**

- Локально: http://localhost:3000/api/docs
- OpenAPI JSON: http://localhost:3000/api/docs-json

Кнопка **Authorize** принимает JWT: получите токен через `POST /api/auth/login`
(или `/api/auth/register`) и вставьте его в поле `bearer`.

## Запуск

```bash
cd backend
npm install
npm run migration:run
npm run migration:seed
npm run start:dev
```
