import { Controller, Get, Param, ParseUUIDPipe } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOkResponse,
  ApiOperation,
  ApiParam,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator.js';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface.js';
import { ApiErrors } from '../common/decorators/api-errors.decorator.js';
import { DownloadUrlDto } from './dto/download-url.dto.js';
import {
  DownloadsService,
  type DownloadUrlResponse,
} from './downloads.service.js';

@ApiTags('Downloads')
@ApiBearerAuth('bearer')
@Controller('downloads')
export class DownloadsController {
  constructor(private readonly downloadsService: DownloadsService) {}

  @Get(':productId')
  @ApiOperation({
    summary: 'Get a short-lived signed download URL for a purchased product',
  })
  @ApiParam({ name: 'productId', format: 'uuid' })
  @ApiOkResponse({ type: DownloadUrlDto })
  @ApiErrors(401, 403, 404)
  getDownload(
    @CurrentUser() user: AuthenticatedUser,
    @Param('productId', ParseUUIDPipe) productId: string,
  ): Promise<DownloadUrlResponse> {
    return this.downloadsService.getDownloadUrl(user.id, productId);
  }
}
