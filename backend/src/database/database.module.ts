import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { buildDataSourceOptions } from './data-source-options.js';

@Module({
  imports: [
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) =>
        buildDataSourceOptions({
          url: config.get<string>('database.url') as string,
          logging: config.get<string>('app.nodeEnv') === 'development',
        }),
    }),
  ],
})
export class DatabaseModule {}
