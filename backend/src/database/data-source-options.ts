import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { DataSourceOptions } from 'typeorm';
import { CartItem } from './entities/cart-item.entity.js';
import { Cart } from './entities/cart.entity.js';
import { OrderItem } from './entities/order-item.entity.js';
import { Order } from './entities/order.entity.js';
import { Product } from './entities/product.entity.js';
import { PromoCode } from './entities/promo-code.entity.js';
import { User } from './entities/user.entity.js';

const currentDir = dirname(fileURLToPath(import.meta.url));
const extension = import.meta.url.endsWith('.ts') ? 'ts' : 'js';

export const entities = [
  User,
  Product,
  PromoCode,
  Cart,
  CartItem,
  Order,
  OrderItem,
];

export interface DataSourceOptionsInput {
  url: string;
  logging?: boolean;
}

export function buildDataSourceOptions({
  url,
  logging = false,
}: DataSourceOptionsInput): DataSourceOptions {
  return {
    type: 'postgres',
    url,
    entities,
    migrations: [join(currentDir, 'migrations', `*.${extension}`)],
    synchronize: false,
    logging: logging ? ['query', 'error'] : ['error'],
  };
}
