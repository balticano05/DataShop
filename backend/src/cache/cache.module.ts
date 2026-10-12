import { Global, Module } from '@nestjs/common';
import { CacheHeaderInterceptor } from './cache-header.interceptor.js';
import { CatalogCacheService } from './catalog-cache.service.js';
import { RedisService } from './redis.service.js';

@Global()
@Module({
  providers: [RedisService, CatalogCacheService, CacheHeaderInterceptor],
  exports: [RedisService, CatalogCacheService, CacheHeaderInterceptor],
})
export class CacheModule {}
