import {
  Column,
  CreateDateColumn,
  Entity,
  Index,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { upperCaseTransformer } from '../transformers/upper-case.transformer.js';

@Entity('promo_codes')
export class PromoCode {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index('UQ_promo_codes_code', { unique: true })
  @Column({
    type: 'varchar',
    length: 64,
    transformer: upperCaseTransformer,
  })
  code: string;

  @Column({ name: 'discount_percentage', type: 'int' })
  discountPercentage: number;

  @Column({ name: 'is_active', type: 'boolean', default: true })
  isActive: boolean;

  @CreateDateColumn({ name: 'created_at', type: 'timestamptz' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at', type: 'timestamptz' })
  updatedAt: Date;
}
