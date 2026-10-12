import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { APP_GUARD } from '@nestjs/core';
import { minutes, ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import { LoggerModule } from 'nestjs-pino';
import { AuthModule } from './auth/auth.module.js';
import { CacheModule } from './cache/cache.module.js';
import { CartModule } from './cart/cart.module.js';
import configuration from './config/configuration.js';
import { envValidationSchema } from './config/env.validation.js';
import { loggerConfig } from './config/logger.config.js';
import { DatabaseModule } from './database/database.module.js';
import { HealthModule } from './health/health.module.js';
import { ProductsModule } from './products/products.module.js';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      cache: true,
      envFilePath: ['../.env', '.env'],
      load: [configuration],
      validationSchema: envValidationSchema,
    }),
    LoggerModule.forRoot(loggerConfig),
    ThrottlerModule.forRoot([{ ttl: minutes(1), limit: 100 }]),
    CacheModule,
    DatabaseModule,
    HealthModule,
    AuthModule,
    ProductsModule,
    CartModule,
  ],
  providers: [{ provide: APP_GUARD, useClass: ThrottlerGuard }],
})
export class AppModule {}
