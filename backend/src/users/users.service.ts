import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { EntityManager, Repository } from 'typeorm';
import { User } from '../database/entities/user.entity.js';
import { UserRole } from '../database/enums/user-role.enum.js';

export interface CreateUserData {
  email: string;
  passwordHash: string;
  role?: UserRole;
}

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private readonly usersRepository: Repository<User>,
  ) {}

  findByEmail(email: string): Promise<User | null> {
    return this.usersRepository.findOne({
      where: { email: email.trim().toLowerCase() },
    });
  }

  findById(id: string): Promise<User | null> {
    return this.usersRepository.findOne({ where: { id } });
  }

  create(data: CreateUserData, manager?: EntityManager): Promise<User> {
    const repository = manager
      ? manager.getRepository(User)
      : this.usersRepository;
    const user = repository.create({
      email: data.email.trim().toLowerCase(),
      passwordHash: data.passwordHash,
      role: data.role ?? UserRole.Customer,
    });
    return repository.save(user);
  }
}
