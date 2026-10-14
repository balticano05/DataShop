import { ApiProperty } from '@nestjs/swagger';

export class UploadedAssetDto {
  @ApiProperty({
    description: 'Cloudinary public id (the only value persisted on the product)',
    example: 'datashop/datasets/a1b2c3d4e5',
  })
  publicId: string;

  @ApiProperty({
    example:
      'https://res.cloudinary.com/demo/raw/private/v1/datashop/datasets/a1b2c3d4e5',
  })
  url: string;
}
