import { Controller, Get, Param, ParseUUIDPipe } from '@nestjs/common';
import { CurrentUser } from '../auth/decorators/current-user.decorator.js';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface.js';
import {
  DownloadsService,
  type DownloadUrlResponse,
} from './downloads.service.js';

@Controller('downloads')
export class DownloadsController {
  constructor(private readonly downloadsService: DownloadsService) {}

  @Get(':productId')
  getDownload(
    @CurrentUser() user: AuthenticatedUser,
    @Param('productId', ParseUUIDPipe) productId: string,
  ): Promise<DownloadUrlResponse> {
    return this.downloadsService.getDownloadUrl(user.id, productId);
  }
}
