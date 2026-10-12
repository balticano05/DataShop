import { UserRole } from '../../database/enums/user-role.enum.js';

export interface AuthenticatedUser {
  id: string;
  email: string;
  role: UserRole;
}

export interface JwtPayload {
  sub: string;
  role: UserRole;
}
