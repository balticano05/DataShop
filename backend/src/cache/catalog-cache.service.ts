import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHash } from 'node:crypto';
import { RedisService } from './redis.service.js';

const KEY_PREFIX = 'catalog:';

@Injectable()
export class CatalogCacheService {
  private readonly logger = new Logger(CatalogCacheService.name);
  private readonly ttlSeconds: number;
  private warned = false;

  constructor(
    private readonly redisService: RedisService,
    config: ConfigService,
  ) {
    this.ttlSeconds = config.get<number>('redis.ttl', 90);
  }

  buildKey(query: Record<string, unknown>): string {
    const normalized = Object.keys(query)
      .filter((key) => {
        const value = query[key];
        return value !== undefined && value !== null && value !== '';
      })
      .sort()
      .map((key) => `${key}=${String(query[key])}`)
      .join('&');
    const hash = createHash('sha1').update(normalized).digest('hex');
    return `${KEY_PREFIX}${hash}`;
  }

  async get<T>(key: string): Promise<T | null> {
    try {
      const raw = await this.redisService.client.get(key);
      return raw ? (JSON.parse(raw) as T) : null;
    } catch (error) {
      this.warnOnce('get', error);
      return null;
    }
  }

  async set(key: string, value: unknown): Promise<void> {
    try {
      await this.redisService.client.set(
        key,
        JSON.stringify(value),
        'EX',
        this.ttlSeconds,
      );
    } catch (error) {
      this.warnOnce('set', error);
    }
  }

  async invalidateAll(): Promise<void> {
    try {
      const client = this.redisService.client;
      let cursor = '0';
      do {
        const [next, keys] = await client.scan(
          cursor,
          'MATCH',
          `${KEY_PREFIX}*`,
          'COUNT',
          100,
        );
        cursor = next;
        if (keys.length > 0) {
          await client.unlink(...keys);
        }
      } while (cursor !== '0');
    } catch (error) {
      this.warnOnce('invalidateAll', error);
    }
  }

  private warnOnce(operation: string, error: unknown): void {
    if (this.warned) {
      return;
    }
    this.warned = true;
    this.logger.warn(
      `Redis unavailable during ${operation}: ${(error as Error).message}`,
    );
  }
}
