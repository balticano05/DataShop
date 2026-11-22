package com.datashop.security;

public interface PasswordEncoder {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
