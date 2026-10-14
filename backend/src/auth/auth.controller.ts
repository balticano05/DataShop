import {
  Body,
  Controller,
  Get,
  HttpCode,
  HttpStatus,
  Post,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { minutes, Throttle } from '@nestjs/throttler';
import { ApiErrors } from '../common/decorators/api-errors.decorator.js';
import { UserRole } from '../database/enums/user-role.enum.js';
import { AuthService, AuthResponse } from './auth.service.js';
import { CurrentUser } from './decorators/current-user.decorator.js';
import { Public } from './decorators/public.decorator.js';
import { Roles } from './decorators/roles.decorator.js';
import {
  AuthResponseDto,
  AuthenticatedUserDto,
} from './dto/auth-response.dto.js';
import { LoginDto } from './dto/login.dto.js';
import { RegisterDto } from './dto/register.dto.js';
import type { AuthenticatedUser } from './interfaces/authenticated-user.interface.js';

@ApiTags('Auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @Throttle({ default: { limit: 5, ttl: minutes(1) } })
  @ApiOperation({ summary: 'Register a new customer' })
  @ApiCreatedResponse({ type: AuthResponseDto })
  @ApiErrors(400, 409, 429)
  @Post('register')
  register(@Body() dto: RegisterDto): Promise<AuthResponse> {
    return this.authService.register(dto);
  }

  @Public()
  @Throttle({ default: { limit: 5, ttl: minutes(1) } })
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Log in and obtain a JWT' })
  @ApiOkResponse({ type: AuthResponseDto })
  @ApiErrors(400, 401, 429)
  @Post('login')
  login(@Body() dto: LoginDto): Promise<AuthResponse> {
    return this.authService.login(dto);
  }

  @ApiBearerAuth('bearer')
  @ApiOperation({ summary: 'Current authenticated user' })
  @ApiOkResponse({ type: AuthenticatedUserDto })
  @ApiErrors(401)
  @Get('me')
  me(@CurrentUser() user: AuthenticatedUser): AuthenticatedUser {
    return user;
  }

  @Roles(UserRole.Admin)
  @ApiBearerAuth('bearer')
  @ApiOperation({ summary: 'Admin-only access probe (temporary)' })
  @ApiOkResponse({ description: 'Admin access granted' })
  @ApiErrors(401, 403)
  @Get('admin-check')
  adminCheck(@CurrentUser() user: AuthenticatedUser) {
    return { message: 'admin access granted', user };
  }
}
