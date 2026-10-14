import { applyDecorators } from '@nestjs/common';
import { ApiResponse } from '@nestjs/swagger';
import { ErrorResponseDto } from '../dto/error-response.dto.js';

const ERROR_DESCRIPTIONS: Record<number, string> = {
  400: 'Bad Request — validation failed',
  401: 'Unauthorized — missing or invalid token',
  403: 'Forbidden — insufficient role or resource not owned',
  404: 'Not Found',
  409: 'Conflict — e.g. insufficient stock',
  429: 'Too Many Requests — rate limit exceeded',
};

export function ApiErrors(...statuses: number[]): MethodDecorator & ClassDecorator {
  const list = statuses.length > 0 ? statuses : [400, 401, 403, 404, 429];
  return applyDecorators(
    ...list.map((status) =>
      ApiResponse({
        status,
        description: ERROR_DESCRIPTIONS[status] ?? 'Error',
        type: ErrorResponseDto,
      }),
    ),
  );
}
