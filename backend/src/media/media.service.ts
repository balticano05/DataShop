import {
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import type { UploadApiOptions, UploadApiResponse } from 'cloudinary';
import { Repository } from 'typeorm';
import { Product } from '../database/entities/product.entity.js';
import { CLOUDINARY, type CloudinaryClient } from './cloudinary.provider.js';
import { DOWNLOAD_URL_TTL_SECONDS, type UploadedFileData } from './upload.constants.js';

export interface UploadedAsset {
  publicId: string;
  url: string;
}

@Injectable()
export class MediaService {
  constructor(
    @Inject(CLOUDINARY)
    private readonly cloudinary: CloudinaryClient,
    @InjectRepository(Product)
    private readonly productsRepository: Repository<Product>,
  ) {}

  async uploadPreview(file: UploadedFileData): Promise<UploadedAsset> {
    const result = await this.upload(file.buffer, {
      folder: 'datashop/previews',
      resource_type: 'image',
      use_filename: true,
      unique_filename: true,
    });
    return { publicId: result.public_id, url: result.secure_url };
  }

  async uploadDataset(
    productId: string,
    file: UploadedFileData,
  ): Promise<UploadedAsset> {
    const product = await this.productsRepository.findOne({
      where: { id: productId },
    });
    if (!product) {
      throw new NotFoundException('Product not found');
    }

    const result = await this.upload(file.buffer, {
      folder: 'datashop/datasets',
      resource_type: 'raw',
      type: 'private',
      use_filename: true,
      unique_filename: true,
    });

    product.filePublicId = result.public_id;
    await this.productsRepository.save(product);

    return { publicId: result.public_id, url: result.secure_url };
  }

  createSignedDownloadUrl(
    publicId: string,
    expiresInSeconds = DOWNLOAD_URL_TTL_SECONDS,
  ): string {
    const expiresAt = Math.floor(Date.now() / 1000) + expiresInSeconds;
    return this.cloudinary.utils.private_download_url(publicId, '', {
      resource_type: 'raw',
      type: 'private',
      expires_at: expiresAt,
    });
  }

  private upload(
    buffer: Buffer,
    options: UploadApiOptions,
  ): Promise<UploadApiResponse> {
    return new Promise((resolve, reject) => {
      const stream = this.cloudinary.uploader.upload_stream(
        options,
        (error, result) => {
          if (error || !result) {
            reject(error ?? new Error('Cloudinary upload failed'));
            return;
          }
          resolve(result);
        },
      );
      stream.end(buffer);
    });
  }
}
