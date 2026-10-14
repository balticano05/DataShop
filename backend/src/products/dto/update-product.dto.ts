import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import {
  ArrayMinSize,
  IsArray,
  IsInt,
  IsNumber,
  IsOptional,
  IsPositive,
  IsString,
  IsUrl,
  Max,
  MaxLength,
  Min,
  MinLength,
} from 'class-validator';

export class UpdateProductDto {
  @ApiPropertyOptional({ example: 'Акции Tesla 2019–2024', minLength: 3, maxLength: 255 })
  @IsOptional()
  @IsString()
  @MinLength(3)
  @MaxLength(255)
  title?: string;

  @ApiPropertyOptional({ example: 'Обновлённое описание набора.' })
  @IsOptional()
  @IsString()
  @MinLength(1)
  description?: string;

  @ApiPropertyOptional({ example: 1299.0, minimum: 0 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @IsPositive()
  price?: number;

  @ApiPropertyOptional({ example: 4.9, minimum: 0, maximum: 5 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(5)
  rating?: number;

  @ApiPropertyOptional({
    description: 'At least 3 image URLs',
    example: [
      'https://picsum.photos/seed/datashop-1-1/800/600',
      'https://picsum.photos/seed/datashop-1-2/800/600',
      'https://picsum.photos/seed/datashop-1-3/800/600',
    ],
    minItems: 3,
  })
  @IsOptional()
  @IsArray()
  @ArrayMinSize(3)
  @IsUrl({}, { each: true })
  images?: string[];

  @ApiPropertyOptional({ example: 30, minimum: 0 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  stockQuantity?: number;

  @ApiPropertyOptional({
    description: 'Cloudinary public id of the dataset file',
    example: 'datashop/datasets/a1b2c3d4e5',
  })
  @IsOptional()
  @IsString()
  @MaxLength(255)
  filePublicId?: string;
}
