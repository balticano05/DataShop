import { describe, expect, it } from 'vitest';
import { escapeLike } from '../src/common/utils/escape-like';

describe('escapeLike (task 123)', () => {
  it('escapes the percent sign', () => {
    expect(escapeLike('50%')).toBe('50\\%');
  });

  it('escapes the underscore', () => {
    expect(escapeLike('a_b')).toBe('a\\_b');
  });

  it('escapes the backslash', () => {
    expect(escapeLike('a\\b')).toBe('a\\\\b');
  });

  it('escapes every special character in a mixed string', () => {
    expect(escapeLike('%_\\')).toBe('\\%\\_\\\\');
  });

  it('leaves ordinary characters untouched', () => {
    expect(escapeLike('Tesla 2019-2024')).toBe('Tesla 2019-2024');
  });

  it('escapes each occurrence, not only the first', () => {
    expect(escapeLike('a%b%c')).toBe('a\\%b\\%c');
  });
});
