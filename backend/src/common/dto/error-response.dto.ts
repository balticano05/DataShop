import { ApiProperty } from '@nestjs/swagger';

export class ErrorResponseDto {
  @ApiProperty({ example: 404, description: 'HTTP status code' })
  statusCode: number;

  @ApiProperty({ example: 'Not Found', description: 'HTTP reason phrase' })
  error: string;

  @ApiProperty({
    description: 'Human-readable message or a list of validation errors',
    oneOf: [{ type: 'string' }, { type: 'array', items: { type: 'string' } }],
    example: 'Product not found',
  })
  message: string | string[];

  @ApiProperty({
    example: '/api/products/00000000-0000-0000-0000-000000000000',
    description: 'Requested path',
  })
  path: string;

  @ApiProperty({
    example: '2026-10-12T19:55:48.284Z',
    description: 'Error timestamp (ISO 8601)',
  })
  timestamp: string;
}
