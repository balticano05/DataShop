export const PREVIEW_MAX_BYTES = 5 * 1024 * 1024;
export const DATASET_MAX_BYTES = 50 * 1024 * 1024;

export const PREVIEW_MIME_TYPES = new Set([
  'image/png',
  'image/jpeg',
  'image/webp',
]);

export const DATASET_MIME_TYPES = new Set([
  'application/zip',
  'application/x-zip-compressed',
  'application/json',
  'text/csv',
  'text/plain',
  'application/gzip',
  'application/x-gzip',
  'application/octet-stream',
]);

export const DOWNLOAD_URL_TTL_SECONDS = 60;

export interface UploadedFileData {
  fieldname: string;
  originalname: string;
  mimetype: string;
  size: number;
  buffer: Buffer;
}

