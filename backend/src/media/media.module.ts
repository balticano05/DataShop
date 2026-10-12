import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Product } from '../database/entities/product.entity.js';
import { cloudinaryProvider } from './cloudinary.provider.js';
import { MediaController } from './media.controller.js';
import { MediaService } from './media.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([Product])],
  controllers: [MediaController],
  providers: [cloudinaryProvider, MediaService],
  exports: [MediaService],
})
export class MediaModule {}
