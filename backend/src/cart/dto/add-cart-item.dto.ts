import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsInt, IsUUID, Max, Min } from 'class-validator';

export class AddCartItemDto {
  @ApiProperty({
    example: '9b2c1d3e-4f5a-6b7c-8d9e-0f1a2b3c4d5e',
    format: 'uuid',
  })
  @IsUUID()
  productId: string;

  @ApiProperty({ example: 1, minimum: 1, maximum: 99, default: 1 })
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(99)
  quantity: number = 1;
}
