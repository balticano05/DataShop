import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { OrderItem } from '../database/entities/order-item.entity.js';
import { Product } from '../database/entities/product.entity.js';
import { MediaModule } from '../media/media.module.js';
import { DownloadsController } from './downloads.controller.js';
import { DownloadsService } from './downloads.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([Product, OrderItem]), MediaModule],
  controllers: [DownloadsController],
  providers: [DownloadsService],
})
export class DownloadsModule {}
