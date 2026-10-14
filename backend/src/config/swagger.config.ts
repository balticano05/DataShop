import type { INestApplication } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { ErrorResponseDto } from '../common/dto/error-response.dto.js';

export const SWAGGER_PATH = 'api/docs';

export function setupSwagger(app: INestApplication): void {
  const config = new DocumentBuilder()
    .setTitle('DataShop API')
    .setDescription(
      'REST API маркетплейса датасетов DataShop: каталог, корзина и промокоды, ' +
        'оформление заказов, загрузка медиа (admin) и защищённое скачивание купленных файлов.',
    )
    .setVersion('1.0')
    .addBearerAuth(
      {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
        description: 'JWT из POST /api/auth/login или /api/auth/register.',
      },
      'bearer',
    )
    .addTag('Auth', 'Регистрация, вход, профиль')
    .addTag('Products', 'Каталог и CRUD товаров')
    .addTag('Cart', 'Корзина и промокоды')
    .addTag('Orders', 'Оформление и просмотр заказов')
    .addTag('Media', 'Загрузка превью и файлов баз (admin)')
    .addTag('Downloads', 'Защищённые ссылки на скачивание')
    .addTag('Health', 'Проверки живости и готовности')
    .build();

  const document = SwaggerModule.createDocument(app, config, {
    extraModels: [ErrorResponseDto],
  });

  SwaggerModule.setup(SWAGGER_PATH, app, document, {
    swaggerOptions: {
      persistAuthorization: true,
      tagsSorter: 'alpha',
      docExpansion: 'none',
    },
  });
}
