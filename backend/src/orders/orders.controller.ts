import {
  Controller,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
} from '@nestjs/common';
import { CurrentUser } from '../auth/decorators/current-user.decorator.js';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface.js';
import { OrderResponseDto } from './dto/order-response.dto.js';
import { OrdersService } from './orders.service.js';

@Controller('orders')
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  @Post('checkout')
  checkout(@CurrentUser() user: AuthenticatedUser): Promise<OrderResponseDto> {
    return this.ordersService.checkout(user.id);
  }

  @Get()
  findMine(
    @CurrentUser() user: AuthenticatedUser,
  ): Promise<OrderResponseDto[]> {
    return this.ordersService.findMine(user.id);
  }

  @Get(':id')
  findOne(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<OrderResponseDto> {
    return this.ordersService.findOne(user.id, id);
  }
}
