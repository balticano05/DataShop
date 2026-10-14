import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  HttpStatus,
  Param,
  ParseUUIDPipe,
  Patch,
  Post,
  Query,
  UseInterceptors,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiNoContentResponse,
  ApiOkResponse,
  ApiOperation,
  ApiParam,
  ApiTags,
} from '@nestjs/swagger';
import { Public } from '../auth/decorators/public.decorator.js';
import { Roles } from '../auth/decorators/roles.decorator.js';
import { CacheHeaderInterceptor } from '../cache/cache-header.interceptor.js';
import { ApiErrors } from '../common/decorators/api-errors.decorator.js';
import { UserRole } from '../database/enums/user-role.enum.js';
import { CreateProductDto } from './dto/create-product.dto.js';
import { PaginatedProductsDto } from './dto/paginated-products.dto.js';
import { ProductResponseDto } from './dto/product-response.dto.js';
import { QueryProductsDto } from './dto/query-products.dto.js';
import { UpdateProductDto } from './dto/update-product.dto.js';
import { PaginatedProducts, ProductsService } from './products.service.js';

@ApiTags('Products')
@Controller('products')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @Public()
  @Get()
  @UseInterceptors(CacheHeaderInterceptor)
  @ApiOperation({ summary: 'List products with filtering, sorting, pagination' })
  @ApiOkResponse({
    type: PaginatedProductsDto,
    description: 'Public; may be served from cache (X-Cache: HIT/MISS)',
  })
  @ApiErrors(400)
  findAll(@Query() query: QueryProductsDto): Promise<PaginatedProducts> {
    return this.productsService.findAll(query);
  }

  @Public()
  @Get(':id')
  @ApiOperation({ summary: 'Get a single product by id' })
  @ApiParam({ name: 'id', format: 'uuid' })
  @ApiOkResponse({ type: ProductResponseDto })
  @ApiErrors(404)
  findOne(
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<ProductResponseDto> {
    return this.productsService.findOne(id);
  }

  @Roles(UserRole.Admin)
  @ApiBearerAuth('bearer')
  @Post()
  @ApiOperation({ summary: 'Create a product (admin)' })
  @ApiCreatedResponse({ type: ProductResponseDto })
  @ApiErrors(400, 401, 403)
  create(@Body() dto: CreateProductDto): Promise<ProductResponseDto> {
    return this.productsService.create(dto);
  }

  @Roles(UserRole.Admin)
  @ApiBearerAuth('bearer')
  @Patch(':id')
  @ApiOperation({ summary: 'Update a product (admin)' })
  @ApiParam({ name: 'id', format: 'uuid' })
  @ApiOkResponse({ type: ProductResponseDto })
  @ApiErrors(400, 401, 403, 404)
  update(
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateProductDto,
  ): Promise<ProductResponseDto> {
    return this.productsService.update(id, dto);
  }

  @Roles(UserRole.Admin)
  @ApiBearerAuth('bearer')
  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({ summary: 'Soft-delete a product (admin)' })
  @ApiParam({ name: 'id', format: 'uuid' })
  @ApiNoContentResponse({ description: 'Product deleted' })
  @ApiErrors(401, 403, 404)
  remove(@Param('id', ParseUUIDPipe) id: string): Promise<void> {
    return this.productsService.remove(id);
  }
}
