import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PromoCode } from '../database/entities/promo-code.entity.js';

@Injectable()
export class PromoService {
  constructor(
    @InjectRepository(PromoCode)
    private readonly promoRepository: Repository<PromoCode>,
  ) {}

  findByCode(code: string): Promise<PromoCode | null> {
    return this.promoRepository.findOne({
      where: { code: code.trim().toUpperCase() },
    });
  }

  async findValid(code: string): Promise<PromoCode> {
    const promo = await this.findByCode(code);
    if (!promo) {
      throw new NotFoundException('Promo code not found');
    }
    if (!promo.isActive) {
      throw new BadRequestException('Promo code is not active');
    }
    return promo;
  }
}
