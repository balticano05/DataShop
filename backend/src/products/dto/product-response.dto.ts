import { ApiProperty } from '@nestjs/swagger';
import { Product } from '../../database/entities/product.entity.js';

export class ProductResponseDto {
  @ApiProperty({ example: '6f1c2f2e-1a2b-4c3d-8e9f-0a1b2c3d4e5f' })
  id: string;

  @ApiProperty({ example: 'Акции Tesla 2019–2024' })
  title: string;

  @ApiProperty({ example: 'Дневные котировки TSLA с объёмами и дивидендами.' })
  description: string;

  @ApiProperty({ example: 1499.0 })
  price: number;

  @ApiProperty({ example: 4.8 })
  rating: number;

  @ApiProperty({
    type: [String],
    example: [
      'https://placehold.co/800x600/1e293b/ffffff/png?text=TSLA&font=montserrat',
      'https://placehold.co/800x600/1e293b/ffffff/png?text=TSLA%0APREVIEW%201&font=montserrat',
      'https://placehold.co/800x600/1e293b/ffffff/png?text=TSLA%0APREVIEW%202&font=montserrat',
    ],
  })
  images: string[];

  @ApiProperty({ example: 25 })
  stockQuantity: number;

  @ApiProperty({ example: '2026-10-12T19:55:48.284Z', format: 'date-time' })
  createdAt: Date;

  @ApiProperty({ example: '2026-10-12T19:55:48.284Z', format: 'date-time' })
  updatedAt: Date;

  static from(product: Product): ProductResponseDto {
    return {
      id: product.id,
      title: product.title,
      description: product.description,
      price: product.price,
      rating: product.rating,
      images: product.images,
      stockQuantity: product.stockQuantity,
      createdAt: product.createdAt,
      updatedAt: product.updatedAt,
    };
  }
}
