import { config } from 'dotenv';
import { DataSource } from 'typeorm';
import { buildDataSourceOptions } from './data-source-options.js';

config({ path: ['../.env', '.env'] });

export default new DataSource(
  buildDataSourceOptions({
    url: process.env.DATABASE_URL as string,
    migrations: true,
  }),
);
