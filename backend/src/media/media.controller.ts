import {
  BadRequestException,
  Controller,
  Param,
  ParseUUIDPipe,
  Post,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import type { Request } from 'express';
import { Roles } from '../auth/decorators/roles.decorator.js';
import { UserRole } from '../database/enums/user-role.enum.js';
import { MediaService, type UploadedAsset } from './media.service.js';
import {
  DATASET_MAX_BYTES,
  DATASET_MIME_TYPES,
  PREVIEW_MAX_BYTES,
  PREVIEW_MIME_TYPES,
  type UploadedFileData,
} from './upload.constants.js';

type FileFilterCallback = (error: Error | null, acceptFile: boolean) => void;

function makeMimeFilter(allowed: Set<string>, label: string) {
  return (
    _req: Request,
    file: UploadedFileData,
    callback: FileFilterCallback,
  ): void => {
    if (allowed.has(file.mimetype)) {
      callback(null, true);
      return;
    }
    callback(
      new BadRequestException(`Unsupported ${label} type: ${file.mimetype}`),
      false,
    );
  };
}

@Controller('admin/media')
@Roles(UserRole.Admin)
export class MediaController {
  constructor(private readonly mediaService: MediaService) {}

  @Post('preview')
  @UseInterceptors(
    FileInterceptor('file', {
      limits: { fileSize: PREVIEW_MAX_BYTES, files: 1 },
      fileFilter: makeMimeFilter(PREVIEW_MIME_TYPES, 'image'),
    }),
  )
  uploadPreview(
    @UploadedFile() file?: UploadedFileData,
  ): Promise<UploadedAsset> {
    if (!file) {
      throw new BadRequestException('File is required (field "file")');
    }
    return this.mediaService.uploadPreview(file);
  }

  @Post('dataset/:productId')
  @UseInterceptors(
    FileInterceptor('file', {
      limits: { fileSize: DATASET_MAX_BYTES, files: 1 },
      fileFilter: makeMimeFilter(DATASET_MIME_TYPES, 'dataset'),
    }),
  )
  uploadDataset(
    @Param('productId', ParseUUIDPipe) productId: string,
    @UploadedFile() file?: UploadedFileData,
  ): Promise<UploadedAsset> {
    if (!file) {
      throw new BadRequestException('File is required (field "file")');
    }
    return this.mediaService.uploadDataset(productId, file);
  }
}
