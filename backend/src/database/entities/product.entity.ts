import {
  Check,
  Column,
  CreateDateColumn,
  DeleteDateColumn,
  Entity,
  Index,
  OneToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { numericTransformer } from '../transformers/numeric.transformer.js';
import { CartItem } from './cart-item.entity.js';
import { OrderItem } from './order-item.entity.js';

@Entity('products')
@Check('CHK_products_stock_quantity', '"stock_quantity" >= 0')
@Index('IDX_products_price', ['price'])
@Index('IDX_products_rating', ['rating'])
@Index('IDX_products_title', ['title'])
export class Product {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ type: 'varchar', length: 255 })
  title: string;

  @Column({ type: 'text' })
  description: string;

  @Column({
    type: 'numeric',
    precision: 10,
    scale: 2,
    transformer: numericTransformer,
  })
  price: number;

  @Column({
    type: 'numeric',
    precision: 2,
    scale: 1,
    default: 0,
    transformer: numericTransformer,
  })
  rating: number;

  @Column({ type: 'text', array: true, default: () => "'{}'" })
  images: string[];

  @Column({ name: 'stock_quantity', type: 'int', default: 0 })
  stockQuantity: number;

  @Column({
    name: 'file_public_id',
    type: 'varchar',
    length: 255,
    nullable: true,
  })
  filePublicId: string | null;

  @CreateDateColumn({ name: 'created_at', type: 'timestamptz' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at', type: 'timestamptz' })
  updatedAt: Date;

  @DeleteDateColumn({ name: 'deleted_at', type: 'timestamptz', nullable: true })
  deletedAt: Date | null;

  @OneToMany(() => CartItem, (item) => item.product)
  cartItems: CartItem[];

  @OneToMany(() => OrderItem, (item) => item.product)
  orderItems: OrderItem[];
}
