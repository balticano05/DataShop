import { ApiProperty } from '@nestjs/swagger';
import { UserRole } from '../../database/enums/user-role.enum.js';

export class AuthenticatedUserDto {
  @ApiProperty({ example: '6f1c2f2e-1a2b-4c3d-8e9f-0a1b2c3d4e5f' })
  id: string;

  @ApiProperty({ example: 'customer@example.com' })
  email: string;

  @ApiProperty({ enum: UserRole, example: UserRole.Customer })
  role: UserRole;
}

export class AuthResponseDto {
  @ApiProperty({
    description: 'Bearer JWT (expires in 1h)',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIuLi4ifQ.signature',
  })
  accessToken: string;

  @ApiProperty({ type: AuthenticatedUserDto })
  user: AuthenticatedUserDto;
}
