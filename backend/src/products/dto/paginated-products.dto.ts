import { ApiProperty } from '@nestjs/swagger';
import { ProductResponseDto } from './product-response.dto.js';

export class ProductsMetaDto {
  @ApiProperty({ example: 21, description: 'Total number of matching products' })
  total: number;

  @ApiProperty({ example: 1 })
  page: number;

  @ApiProperty({ example: 20 })
  limit: number;

  @ApiProperty({ example: 2, description: 'Total number of pages' })
  totalPages: number;
}

export class PaginatedProductsDto {
  @ApiProperty({ type: [ProductResponseDto] })
  items: ProductResponseDto[];

  @ApiProperty({ type: ProductsMetaDto })
  meta: ProductsMetaDto;
}
