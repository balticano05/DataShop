import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { LoggerModule } from 'nestjs-pino';
import configuration from './config/configuration.js';
import { envValidationSchema } from './config/env.validation.js';
import { loggerConfig } from './config/logger.config.js';
import { DatabaseModule } from './database/database.module.js';
import { HealthModule } from './health/health.module.js';

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
    DatabaseModule,
    HealthModule,
  ],
})
export class AppModule {}
