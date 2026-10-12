import { Product } from '../../database/entities/product.entity.js';

export class ProductResponseDto {
  id: string;
  title: string;
  description: string;
  price: number;
  rating: number;
  images: string[];
  stockQuantity: number;
  createdAt: Date;
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
