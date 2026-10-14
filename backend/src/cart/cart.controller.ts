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
import {
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiOperation,
  ApiParam,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator.js';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface.js';
import { ApiErrors } from '../common/decorators/api-errors.decorator.js';
import { CartService } from './cart.service.js';
import { AddCartItemDto } from './dto/add-cart-item.dto.js';
import { ApplyPromoDto } from './dto/apply-promo.dto.js';
import { CartResponseDto } from './dto/cart-response.dto.js';
import { UpdateCartItemDto } from './dto/update-cart-item.dto.js';

@ApiTags('Cart')
@ApiBearerAuth('bearer')
@Controller('cart')
export class CartController {
  constructor(private readonly cartService: CartService) {}

  @Get()
  @ApiOperation({ summary: 'Get the current cart' })
  @ApiOkResponse({ type: CartResponseDto })
  @ApiErrors(401)
  getCart(@CurrentUser() user: AuthenticatedUser): Promise<CartResponseDto> {
    return this.cartService.getCart(user.id);
  }

  @Post('items')
  @ApiOperation({ summary: 'Add a product to the cart' })
  @ApiCreatedResponse({ type: CartResponseDto })
  @ApiErrors(400, 401, 404, 409)
  addItem(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: AddCartItemDto,
  ): Promise<CartResponseDto> {
    return this.cartService.addItem(user.id, dto);
  }

  @Patch('items/:id')
  @ApiOperation({ summary: 'Update cart item quantity' })
  @ApiParam({ name: 'id', description: 'Cart item id', format: 'uuid' })
  @ApiOkResponse({ type: CartResponseDto })
  @ApiErrors(400, 401, 404)
  updateItem(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateCartItemDto,
  ): Promise<CartResponseDto> {
    return this.cartService.updateItem(user.id, id, dto);
  }

  @Delete('items/:id')
  @ApiOperation({ summary: 'Remove an item from the cart' })
  @ApiParam({ name: 'id', description: 'Cart item id', format: 'uuid' })
  @ApiOkResponse({ type: CartResponseDto })
  @ApiErrors(401, 404)
  removeItem(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<CartResponseDto> {
    return this.cartService.removeItem(user.id, id);
  }

  @Post('apply-promo')
  @ApiOperation({ summary: 'Apply a promo code to the cart' })
  @ApiCreatedResponse({ type: CartResponseDto })
  @ApiErrors(400, 401, 404)
  applyPromo(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: ApplyPromoDto,
  ): Promise<CartResponseDto> {
    return this.cartService.applyPromo(user.id, dto);
  }
}
