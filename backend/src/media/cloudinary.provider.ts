import { ConfigService } from '@nestjs/config';
import { v2 as cloudinary } from 'cloudinary';
import type { Provider } from '@nestjs/common';
import type { CloudinaryConfig } from '../config/configuration.js';

export const CLOUDINARY = 'CLOUDINARY';

export type CloudinaryClient = typeof cloudinary;

export const cloudinaryProvider: Provider = {
  provide: CLOUDINARY,
  inject: [ConfigService],
  useFactory: (config: ConfigService): CloudinaryClient => {
    const cfg = config.get<CloudinaryConfig>('cloudinary');
    cloudinary.config({
      cloud_name: cfg?.cloudName ?? process.env.CLOUDINARY_CLOUD_NAME,
      api_key: cfg?.apiKey ?? process.env.CLOUDINARY_API_KEY,
      api_secret: cfg?.apiSecret ?? process.env.CLOUDINARY_API_SECRET,
      secure: true,
    });
    return cloudinary;
  },
};
