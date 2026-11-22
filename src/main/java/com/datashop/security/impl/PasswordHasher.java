package com.datashop.security.impl;

import com.datashop.security.PasswordEncoder;
import com.datashop.util.Constants;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher implements PasswordEncoder {

    private final Argon2 argon2;
    private final int iterations;
    private final int memoryKb;
    private final int parallelism;

    public PasswordHasher() {
        this(
                Constants.Security.ARGON2_ITERATIONS,
                Constants.Security.ARGON2_MEMORY_KB,
                Constants.Security.ARGON2_PARALLELISM
        );
    }

    public PasswordHasher(int iterations, int memoryKb, int parallelism) {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        this.iterations = iterations;
        this.memoryKb = memoryKb;
        this.parallelism = parallelism;
    }

    @Override
    public String hash(String rawPassword) {
        char[] password = rawPassword.toCharArray();
        try {
            return argon2.hash(iterations, memoryKb, parallelism, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        if (rawPassword == null || hash == null || hash.isBlank()) {
            return false;
        }
        char[] password = rawPassword.toCharArray();
        try {
            return argon2.verify(hash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }
}
