import {
  ConflictException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import argon2 from 'argon2';
import { DataSource } from 'typeorm';
import { Cart } from '../database/entities/cart.entity.js';
import { User } from '../database/entities/user.entity.js';
import { UserRole } from '../database/enums/user-role.enum.js';
import { UsersService } from '../users/users.service.js';
import { LoginDto } from './dto/login.dto.js';
import { RegisterDto } from './dto/register.dto.js';
import type {
  AuthenticatedUser,
  JwtPayload,
} from './interfaces/authenticated-user.interface.js';

const INVALID_CREDENTIALS = 'Invalid email or password';

export interface AuthResponse {
  accessToken: string;
  user: AuthenticatedUser;
}

@Injectable()
export class AuthService {
  constructor(
    private readonly usersService: UsersService,
    private readonly jwtService: JwtService,
    private readonly dataSource: DataSource,
  ) {}

  async register(dto: RegisterDto): Promise<AuthResponse> {
    const existing = await this.usersService.findByEmail(dto.email);
    if (existing) {
      throw new ConflictException('Email is already registered');
    }

    const passwordHash = await argon2.hash(dto.password);
    const user = await this.dataSource.transaction(async (manager) => {
      const created = await this.usersService.create(
        { email: dto.email, passwordHash, role: UserRole.Customer },
        manager,
      );
      await manager.save(manager.create(Cart, { userId: created.id }));
      return created;
    });

    return this.buildAuthResponse(user);
  }

  async login(dto: LoginDto): Promise<AuthResponse> {
    const user = await this.usersService.findByEmail(dto.email);
    const passwordMatches = user
      ? await this.verifyPassword(user.passwordHash, dto.password)
      : false;

    if (!user || !passwordMatches) {
      throw new UnauthorizedException(INVALID_CREDENTIALS);
    }

    return this.buildAuthResponse(user);
  }

  private async verifyPassword(
    passwordHash: string,
    password: string,
  ): Promise<boolean> {
    try {
      return await argon2.verify(passwordHash, password);
    } catch {
      return false;
    }
  }

  private buildAuthResponse(user: User): AuthResponse {
    const payload: JwtPayload = { sub: user.id, role: user.role };
    return {
      accessToken: this.jwtService.sign(payload),
      user: { id: user.id, email: user.email, role: user.role },
    };
  }
}
