import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PromoCode } from '../database/entities/promo-code.entity.js';
import { PromoService } from './promo.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([PromoCode])],
  providers: [PromoService],
  exports: [PromoService],
})
export class PromoModule {}
