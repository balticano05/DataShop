import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { escapeLike } from '../common/utils/escape-like.js';
import { Product } from '../database/entities/product.entity.js';
import { CreateProductDto } from './dto/create-product.dto.js';
import { ProductResponseDto } from './dto/product-response.dto.js';
import { QueryProductsDto } from './dto/query-products.dto.js';
import { UpdateProductDto } from './dto/update-product.dto.js';
import { ProductSortField, SortOrder } from './enums/product-sort.enum.js';

export interface ProductsMeta {
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

export interface PaginatedProducts {
  items: ProductResponseDto[];
  meta: ProductsMeta;
}

const SORT_COLUMNS: Record<ProductSortField, string> = {
  [ProductSortField.Price]: 'product.price',
  [ProductSortField.Title]: 'product.title',
  [ProductSortField.Rating]: 'product.rating',
  [ProductSortField.CreatedAt]: 'product.createdAt',
};

@Injectable()
export class ProductsService {
  constructor(
    @InjectRepository(Product)
    private readonly productsRepository: Repository<Product>,
  ) {}

  async findAll(query: QueryProductsDto): Promise<PaginatedProducts> {
    if (
      query.min_price !== undefined &&
      query.max_price !== undefined &&
      query.min_price > query.max_price
    ) {
      throw new BadRequestException(
        'min_price must not be greater than max_price',
      );
    }

    const page = query.page ?? 1;
    const limit = query.limit ?? 20;
    const sortBy = query.sort_by ?? ProductSortField.CreatedAt;
    const order = (query.order ?? SortOrder.Desc) === SortOrder.Asc
      ? 'ASC'
      : 'DESC';

    const builder = this.productsRepository.createQueryBuilder('product');

    if (query.search) {
      builder.andWhere(
        '(product.title ILIKE :search OR product.description ILIKE :search)',
        { search: `%${escapeLike(query.search.trim())}%` },
      );
    }
    if (query.min_price !== undefined) {
      builder.andWhere('product.price >= :minPrice', {
        minPrice: query.min_price,
      });
    }
    if (query.max_price !== undefined) {
      builder.andWhere('product.price <= :maxPrice', {
        maxPrice: query.max_price,
      });
    }
    if (query.min_rating !== undefined) {
      builder.andWhere('product.rating >= :minRating', {
        minRating: query.min_rating,
      });
    }

    builder
      .orderBy(SORT_COLUMNS[sortBy], order)
      .addOrderBy('product.id', 'ASC')
      .skip((page - 1) * limit)
      .take(limit);

    const [rows, total] = await builder.getManyAndCount();

    return {
      items: rows.map((product) => ProductResponseDto.from(product)),
      meta: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
      },
    };
  }

  async findOne(id: string): Promise<ProductResponseDto> {
    const product = await this.productsRepository.findOne({ where: { id } });
    if (!product) {
      throw new NotFoundException('Product not found');
    }
    return ProductResponseDto.from(product);
  }

  async create(dto: CreateProductDto): Promise<ProductResponseDto> {
    const product = this.productsRepository.create({
      title: dto.title,
      description: dto.description,
      price: dto.price,
      rating: dto.rating ?? 0,
      images: dto.images,
      stockQuantity: dto.stockQuantity ?? 0,
      filePublicId: dto.filePublicId ?? null,
    });
    const saved = await this.productsRepository.save(product);
    return ProductResponseDto.from(saved);
  }

  async update(id: string, dto: UpdateProductDto): Promise<ProductResponseDto> {
    const product = await this.productsRepository.findOne({ where: { id } });
    if (!product) {
      throw new NotFoundException('Product not found');
    }

    if (dto.title !== undefined) product.title = dto.title;
    if (dto.description !== undefined) product.description = dto.description;
    if (dto.price !== undefined) product.price = dto.price;
    if (dto.rating !== undefined) product.rating = dto.rating;
    if (dto.images !== undefined) product.images = dto.images;
    if (dto.stockQuantity !== undefined) product.stockQuantity = dto.stockQuantity;
    if (dto.filePublicId !== undefined) product.filePublicId = dto.filePublicId;

    const saved = await this.productsRepository.save(product);
    return ProductResponseDto.from(saved);
  }

  async remove(id: string): Promise<void> {
    const product = await this.productsRepository.findOne({ where: { id } });
    if (!product) {
      throw new NotFoundException('Product not found');
    }
    await this.productsRepository.softRemove(product);
  }
}
