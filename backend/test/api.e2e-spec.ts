import { INestApplication, ValidationPipe } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import { randomUUID } from 'node:crypto';
import request from 'supertest';
import { DataSource } from 'typeorm';
import { afterAll, beforeAll, describe, expect, it } from 'vitest';
import { AppModule } from '../src/app.module';
import { AllExceptionsFilter } from '../src/common/filters/all-exceptions.filter';

const ADMIN_EMAIL = 'admin@datashop.local';
const ADMIN_PASSWORD = process.env.SEED_ADMIN_PASSWORD ?? 'Admin12345!';
const PRODUCT_PRICE = 1000;
const PROMO_CODE = 'SAVE10';
const RUN = Date.now();

interface Creds {
  token: string;
  id: string;
}

describe('DataShop API (e2e, tasks 126-130)', () => {
  let app: INestApplication;
  let server: unknown;
  let dataSource: DataSource;

  let adminToken = '';
  let productId = '';
  let buyer: Creds;
  let other: Creds;

  const api = () => request(server as Parameters<typeof request>[0]);

  beforeAll(async () => {
    const moduleRef = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleRef.createNestApplication();
    app.setGlobalPrefix('api');
    app.useGlobalPipes(
      new ValidationPipe({
        whitelist: true,
        forbidNonWhitelisted: true,
        transform: true,
        transformOptions: { enableImplicitConversion: true },
      }),
    );
    app.useGlobalFilters(new AllExceptionsFilter());
    await app.init();

    server = app.getHttpServer();
    dataSource = app.get(DataSource);

    const adminLogin = await api()
      .post('/api/auth/login')
      .send({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD })
      .expect(200);
    adminToken = adminLogin.body.accessToken;

    const created = await api()
      .post('/api/products')
      .set('Authorization', `Bearer ${adminToken}`)
      .send({
        title: `E2E dataset ${RUN}`,
        description: 'Temporary product created by the e2e suite.',
        price: PRODUCT_PRICE,
        rating: 4.5,
        stockQuantity: 10,
        images: [1, 2, 3].map(
          (n) => `https://picsum.photos/seed/e2e-${RUN}-${n}/800/600`,
        ),
      })
      .expect(201);
    productId = created.body.id;

    await dataSource.query('UPDATE products SET file_public_id = $1 WHERE id = $2', [
      'datashop/datasets/e2e-placeholder',
      productId,
    ]);

    const buyerReg = await api()
      .post('/api/auth/register')
      .send({ email: `e2e_buyer_${RUN}@test.local`, password: 'BuyerPass123' })
      .expect(201);
    buyer = { token: buyerReg.body.accessToken, id: buyerReg.body.user.id };

    const otherReg = await api()
      .post('/api/auth/register')
      .send({ email: `e2e_other_${RUN}@test.local`, password: 'OtherPass123' })
      .expect(201);
    other = { token: otherReg.body.accessToken, id: otherReg.body.user.id };
  });

  afterAll(async () => {
    if (dataSource?.isInitialized) {
      await dataSource.query('DELETE FROM users WHERE email LIKE $1', ['e2e\\_%']);
      if (productId) {
        await dataSource.query('DELETE FROM products WHERE id = $1', [productId]);
      }
    }
    await app?.close();
  });

  describe('126. auth', () => {
    it('registers a customer and returns a token + user', () => {
      expect(buyer.token).toBeTruthy();
      expect(buyer.id).toBeTruthy();
    });

    it('logs in and returns a token', async () => {
      const res = await api()
        .post('/api/auth/login')
        .send({ email: `e2e_buyer_${RUN}@test.local`, password: 'BuyerPass123' })
        .expect(200);
      expect(res.body.accessToken).toBeTruthy();
      expect(res.body.user.role).toBe('customer');
    });

    it('returns the profile for /me', async () => {
      const res = await api()
        .get('/api/auth/me')
        .set('Authorization', `Bearer ${buyer.token}`)
        .expect(200);
      expect(res.body.id).toBe(buyer.id);
    });

    it('rejects /me without a token (401)', async () => {
      await api().get('/api/auth/me').expect(401);
    });
  });

  describe('127. RBAC', () => {
    it('forbids a customer from creating products (403)', async () => {
      await api()
        .post('/api/products')
        .set('Authorization', `Bearer ${buyer.token}`)
        .send({
          title: 'should not be created',
          description: 'nope',
          price: 10,
          images: ['https://a/1', 'https://a/2', 'https://a/3'],
        })
        .expect(403);
    });

    it('rejects anonymous access to admin routes (401)', async () => {
      await api().post('/api/products').send({}).expect(401);
    });
  });

  describe('128. catalog', () => {
    it('filters, sorts and paginates with a meta block', async () => {
      const res = await api()
        .get('/api/products')
        .query({ sort_by: 'price', order: 'asc', page: 1, limit: 5 })
        .expect(200);

      expect(Array.isArray(res.body.items)).toBe(true);
      expect(res.body.items.length).toBeLessThanOrEqual(5);
      expect(res.body.meta).toMatchObject({ page: 1, limit: 5 });
      expect(typeof res.body.meta.total).toBe('number');
      expect(typeof res.body.meta.totalPages).toBe('number');

      const prices = res.body.items.map((p: { price: number }) => p.price);
      expect(prices).toEqual([...prices].sort((a, b) => a - b));
    });

    it('honours the min_price filter', async () => {
      const res = await api()
        .get('/api/products')
        .query({ min_price: PRODUCT_PRICE })
        .expect(200);
      for (const p of res.body.items as { price: number }[]) {
        expect(p.price).toBeGreaterThanOrEqual(PRODUCT_PRICE);
      }
    });
  });

  describe('129. cart and promo', () => {
    it('adds an item to the cart', async () => {
      const res = await api()
        .post('/api/cart/items')
        .set('Authorization', `Bearer ${buyer.token}`)
        .send({ productId, quantity: 2 })
        .expect(201);

      expect(res.body.items).toHaveLength(1);
      expect(res.body.items[0].productId).toBe(productId);
      expect(res.body.itemCount).toBe(2);
      expect(res.body.subtotal).toBe(PRODUCT_PRICE * 2);
    });

    it('applies SAVE10 for a 10% discount', async () => {
      const res = await api()
        .post('/api/cart/apply-promo')
        .set('Authorization', `Bearer ${buyer.token}`)
        .send({ code: PROMO_CODE })
        .expect(201);

      const subtotal = PRODUCT_PRICE * 2;
      expect(res.body.promoCode).toMatchObject({ code: PROMO_CODE });
      expect(res.body.discount).toBe(subtotal * 0.1);
      expect(res.body.total).toBe(subtotal * 0.9);
    });
  });

  describe('130. checkout and download', () => {
    it('checks out the cart into a paid order', async () => {
      const res = await api()
        .post('/api/orders/checkout')
        .set('Authorization', `Bearer ${buyer.token}`)
        .expect(201);

      expect(res.body.status).toBe('paid');
      expect(res.body.total).toBe(PRODUCT_PRICE * 2 * 0.9);
      expect(res.body.items).toHaveLength(1);
    });

    it('issues a signed download URL that expires in a minute', async () => {
      const res = await api()
        .get(`/api/downloads/${productId}`)
        .set('Authorization', `Bearer ${buyer.token}`)
        .expect(200);

      expect(res.body.expiresIn).toBe(60);
      expect(res.body.url).toContain('signature=');
      expect(res.body.url).toContain('expires_at=');
    });

    it('forbids a non-buyer (403)', async () => {
      await api()
        .get(`/api/downloads/${productId}`)
        .set('Authorization', `Bearer ${other.token}`)
        .expect(403);
    });

    it('returns 404 for an unknown product', async () => {
      await api()
        .get(`/api/downloads/${randomUUID()}`)
        .set('Authorization', `Bearer ${buyer.token}`)
        .expect(404);
    });
  });
});
