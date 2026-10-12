import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseUUIDPipe,
  Patch,
  Post,
} from '@nestjs/common';
import { CurrentUser } from '../auth/decorators/current-user.decorator.js';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface.js';
import { CartService } from './cart.service.js';
import { AddCartItemDto } from './dto/add-cart-item.dto.js';
import { ApplyPromoDto } from './dto/apply-promo.dto.js';
import { CartResponseDto } from './dto/cart-response.dto.js';
import { UpdateCartItemDto } from './dto/update-cart-item.dto.js';

@Controller('cart')
export class CartController {
  constructor(private readonly cartService: CartService) {}

  @Get()
  getCart(@CurrentUser() user: AuthenticatedUser): Promise<CartResponseDto> {
    return this.cartService.getCart(user.id);
  }

  @Post('items')
  addItem(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: AddCartItemDto,
  ): Promise<CartResponseDto> {
    return this.cartService.addItem(user.id, dto);
  }

  @Patch('items/:id')
  updateItem(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateCartItemDto,
  ): Promise<CartResponseDto> {
    return this.cartService.updateItem(user.id, id, dto);
  }

  @Delete('items/:id')
  removeItem(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<CartResponseDto> {
    return this.cartService.removeItem(user.id, id);
  }

  @Post('apply-promo')
  applyPromo(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: ApplyPromoDto,
  ): Promise<CartResponseDto> {
    return this.cartService.applyPromo(user.id, dto);
  }
}
