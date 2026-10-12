import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CartItem } from '../database/entities/cart-item.entity.js';
import { Cart } from '../database/entities/cart.entity.js';
import { Product } from '../database/entities/product.entity.js';
import { PromoModule } from '../promo/promo.module.js';
import { CartController } from './cart.controller.js';
import { CartService } from './cart.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([Cart, CartItem, Product]), PromoModule],
  controllers: [CartController],
  providers: [CartService],
  exports: [CartService],
})
export class CartModule {}
