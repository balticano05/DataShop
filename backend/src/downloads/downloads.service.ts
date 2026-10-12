import {
  ForbiddenException,
  Injectable,
  Logger,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { OrderItem } from '../database/entities/order-item.entity.js';
import { Product } from '../database/entities/product.entity.js';
import { OrderStatus } from '../database/enums/order-status.enum.js';
import { MediaService } from '../media/media.service.js';
import { DOWNLOAD_URL_TTL_SECONDS } from '../media/upload.constants.js';

export interface DownloadUrlResponse {
  url: string;
  expiresIn: number;
}

@Injectable()
export class DownloadsService {
  private readonly logger = new Logger(DownloadsService.name);

  constructor(
    @InjectRepository(Product)
    private readonly productsRepository: Repository<Product>,
    @InjectRepository(OrderItem)
    private readonly orderItemsRepository: Repository<OrderItem>,
    private readonly mediaService: MediaService,
  ) {}

  async getDownloadUrl(
    userId: string,
    productId: string,
  ): Promise<DownloadUrlResponse> {
    const product = await this.productsRepository.findOne({
      where: { id: productId },
    });
    if (!product) {
      throw new NotFoundException('Product not found');
    }

    const purchased = await this.orderItemsRepository
      .createQueryBuilder('item')
      .innerJoin('item.order', 'order')
      .where('item.productId = :productId', { productId })
      .andWhere('order.userId = :userId', { userId })
      .andWhere('order.status = :status', { status: OrderStatus.Paid })
      .getCount();
    if (purchased === 0) {
      throw new ForbiddenException('Product was not purchased');
    }

    if (!product.filePublicId) {
      throw new NotFoundException('File is not available for this product');
    }

    const url = this.mediaService.createSignedDownloadUrl(product.filePublicId);
    this.logger.log(
      `download link issued user=${userId} product=${productId} at=${new Date().toISOString()}`,
    );

    return { url, expiresIn: DOWNLOAD_URL_TTL_SECONDS };
  }
}
