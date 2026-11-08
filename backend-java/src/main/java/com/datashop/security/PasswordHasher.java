package com.datashop.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher {

    private final Argon2 argon2;
    private final int iterations;
    private final int memoryKb;
    private final int parallelism;

    public PasswordHasher() {
        this(3, 65536, 1);
    }

    public PasswordHasher(int iterations, int memoryKb, int parallelism) {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        this.iterations = iterations;
        this.memoryKb = memoryKb;
        this.parallelism = parallelism;
    }

    public String hash(String rawPassword) {
        char[] password = rawPassword.toCharArray();
        try {
            return argon2.hash(iterations, memoryKb, parallelism, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

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
