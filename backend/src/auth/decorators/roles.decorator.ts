import { SetMetadata } from '@nestjs/common';
import { UserRole } from '../../database/enums/user-role.enum.js';

export const ROLES_KEY: string = 'roles';

export const Roles = (...roles: UserRole[]) => SetMetadata(ROLES_KEY, roles);
