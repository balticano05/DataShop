import { ConfigService } from '@nestjs/config';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CatalogCacheService } from '../src/cache/catalog-cache.service';
import { RedisService } from '../src/cache/redis.service';

function makeService(): {
  service: CatalogCacheService;
  client: {
    get: ReturnType<typeof vi.fn>;
    set: ReturnType<typeof vi.fn>;
    scan: ReturnType<typeof vi.fn>;
    unlink: ReturnType<typeof vi.fn>;
  };
} {
  const client = {
    get: vi.fn(),
    set: vi.fn(),
    scan: vi.fn(),
    unlink: vi.fn(),
  };
  const redisService = { client } as unknown as RedisService;
  const config = { get: () => 90 } as unknown as ConfigService;
  return { service: new CatalogCacheService(redisService, config), client };
}

describe('CatalogCacheService.buildKey (task 124)', () => {
  const { service } = makeService();

  it('produces the same key regardless of parameter order', () => {
    const a = service.buildKey({ search: 'tesla', page: 2, sort_by: 'price' });
    const b = service.buildKey({ sort_by: 'price', page: 2, search: 'tesla' });
    expect(a).toBe(b);
  });

  it('produces different keys for different values', () => {
    expect(service.buildKey({ page: 1 })).not.toBe(
      service.buildKey({ page: 2 }),
    );
  });

  it('ignores undefined, null and empty values', () => {
    const sparse = service.buildKey({ page: 1, search: undefined, order: null });
    const plain = service.buildKey({ page: 1 });
    expect(sparse).toBe(plain);
    expect(service.buildKey({ search: '' })).toBe(service.buildKey({}));
  });

  it('namespaces keys with the catalog prefix', () => {
    expect(service.buildKey({ page: 1 }).startsWith('catalog:')).toBe(true);
  });
});

describe('CatalogCacheService (task 125, mocked ioredis)', () => {
  let ctx: ReturnType<typeof makeService>;

  beforeEach(() => {
    ctx = makeService();
  });

  it('get parses cached JSON', async () => {
    ctx.client.get.mockResolvedValue(JSON.stringify({ items: [1, 2] }));
    await expect(ctx.service.get('catalog:x')).resolves.toEqual({
      items: [1, 2],
    });
  });

  it('get returns null on a cache miss', async () => {
    ctx.client.get.mockResolvedValue(null);
    await expect(ctx.service.get('catalog:x')).resolves.toBeNull();
  });

  it('set writes JSON with the configured TTL', async () => {
    ctx.client.set.mockResolvedValue('OK');
    await ctx.service.set('catalog:x', { a: 1 });
    expect(ctx.client.set).toHaveBeenCalledWith(
      'catalog:x',
      JSON.stringify({ a: 1 }),
      'EX',
      90,
    );
  });

  it('degrades gracefully when redis is down', async () => {
    ctx.client.get.mockRejectedValue(new Error('connection refused'));
    await expect(ctx.service.get('catalog:x')).resolves.toBeNull();
  });

  it('invalidateAll scans the whole keyspace and unlinks matches', async () => {
    ctx.client.scan
      .mockResolvedValueOnce(['17', ['catalog:a', 'catalog:b']])
      .mockResolvedValueOnce(['0', ['catalog:c']]);
    ctx.client.unlink.mockResolvedValue(1);

    await ctx.service.invalidateAll();

    expect(ctx.client.scan).toHaveBeenCalledTimes(2);
    expect(ctx.client.unlink).toHaveBeenNthCalledWith(1, 'catalog:a', 'catalog:b');
    expect(ctx.client.unlink).toHaveBeenNthCalledWith(2, 'catalog:c');
  });
});
