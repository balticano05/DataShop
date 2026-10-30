package com.datashop.security;

import com.datashop.web.Errors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class DownloadSigner {

    private final byte[] secret;

    public DownloadSigner(String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String issue(UUID productId, Duration ttl) {
        Ticket ticket = issueTicket(productId, ttl);
        return ticket.productId() + "." + ticket.expiresAt() + "." + ticket.signature();
    }

    public Ticket issueTicket(UUID productId, Duration ttl) {
        long expiresAt = Instant.now().plus(ttl).getEpochSecond();
        String payload = productId + "." + expiresAt;
        return new Ticket(productId, expiresAt, sign(payload));
    }

    public UUID verify(UUID productId, long expiresAt, String signature) {
        if (productId == null || signature == null || signature.isBlank()) {
            throw Errors.forbidden("Invalid or expired download link");
        }
        String payload = productId + "." + expiresAt;
        if (!constantTimeEquals(sign(payload), signature)) {
            throw Errors.forbidden("Invalid or expired download link");
        }
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw Errors.forbidden("Download link expired");
        }
        return productId;
    }

    public UUID verify(String token) {
        if (token == null || token.isBlank()) {
            throw Errors.forbidden("Invalid or expired download link");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw Errors.forbidden("Invalid or expired download link");
        }
        String payload = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(payload), parts[2])) {
            throw Errors.forbidden("Invalid or expired download link");
        }
        long expiresAt;
        UUID productId;
        try {
            productId = UUID.fromString(parts[0]);
            expiresAt = Long.parseLong(parts[1]);
        } catch (IllegalArgumentException exception) {
            throw Errors.forbidden("Invalid or expired download link");
        }
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw Errors.forbidden("Download link expired");
        }
        return productId;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Cannot sign download token", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    public record Ticket(UUID productId, long expiresAt, String signature) {
    }
}
