import { ConfigService } from '@nestjs/config';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module.js';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.setGlobalPrefix('api');
  app.enableCors();

  const configService = app.get(ConfigService);
  await app.listen(configService.get<number>('PORT', 3000));
}
await bootstrap();
