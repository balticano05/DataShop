import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CartItem } from '../database/entities/cart-item.entity.js';
import { Cart } from '../database/entities/cart.entity.js';
import { OrderItem } from '../database/entities/order-item.entity.js';
import { Order } from '../database/entities/order.entity.js';
import { PromoCode } from '../database/entities/promo-code.entity.js';
import { OrdersController } from './orders.controller.js';
import { OrdersService } from './orders.service.js';
import { PaymentsService } from './payments.service.js';

@Module({
  imports: [
    TypeOrmModule.forFeature([Order, OrderItem, Cart, CartItem, PromoCode]),
  ],
  controllers: [OrdersController],
  providers: [OrdersService, PaymentsService],
  exports: [OrdersService, PaymentsService],
})
export class OrdersModule {}
