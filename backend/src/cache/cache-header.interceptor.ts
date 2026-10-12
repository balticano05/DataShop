import {
  CallHandler,
  ExecutionContext,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { from, Observable, of, switchMap } from 'rxjs';
import { CatalogCacheService } from './catalog-cache.service.js';

@Injectable()
export class CacheHeaderInterceptor implements NestInterceptor {
  constructor(private readonly catalogCache: CatalogCacheService) {}

  intercept(
    context: ExecutionContext,
    next: CallHandler,
  ): Observable<unknown> {
    const http = context.switchToHttp();
    const request = http.getRequest();
    const response = http.getResponse();
    const key = this.catalogCache.buildKey(
      (request.query ?? {}) as Record<string, unknown>,
    );

    return from(this.catalogCache.get<unknown>(key)).pipe(
      switchMap((cached) => {
        if (cached !== null) {
          response.setHeader('X-Cache', 'HIT');
          return of(cached);
        }
        return next.handle().pipe(
          switchMap(async (data) => {
            response.setHeader('X-Cache', 'MISS');
            await this.catalogCache.set(key, data);
            return data;
          }),
        );
      }),
    );
  }
}
