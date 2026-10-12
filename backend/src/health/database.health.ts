import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { HealthIndicatorService } from '@nestjs/terminus';
import pg from 'pg';

const { Client } = pg;

@Injectable()
export class DatabaseHealthIndicator {
  constructor(
    private readonly config: ConfigService,
    private readonly healthIndicator: HealthIndicatorService,
  ) {}

  async isHealthy(key: string) {
    const indicator = this.healthIndicator.check(key);
    const client = new Client({
      connectionString: this.config.get<string>('database.url'),
      connectionTimeoutMillis: 3000,
    });

    try {
      await client.connect();
      await client.query('SELECT 1');
      return indicator.up();
    } catch (error) {
      return indicator.down({ message: (error as Error).message });
    } finally {
      await client.end().catch(() => undefined);
    }
  }
}
