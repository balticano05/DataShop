import type { ValueTransformer } from 'typeorm';

export const upperCaseTransformer: ValueTransformer = {
  to: (value?: string | null): string | null | undefined =>
    typeof value === 'string' ? value.toUpperCase() : value,
  from: (value?: string | null): string | null | undefined => value,
};
