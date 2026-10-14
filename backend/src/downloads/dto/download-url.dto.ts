import { ApiProperty } from '@nestjs/swagger';

export class DownloadUrlDto {
  @ApiProperty({
    description: 'Short-lived signed Cloudinary URL',
    example:
      'https://res.cloudinary.com/demo/raw/private/v1/datashop/datasets/a1b2c3d4e5?_s=...&expires_at=...&signature=...',
  })
  url: string;

  @ApiProperty({ example: 60, description: 'URL lifetime in seconds' })
  expiresIn: number;
}
