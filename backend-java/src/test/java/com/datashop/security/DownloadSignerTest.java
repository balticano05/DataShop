package com.datashop.security;

import com.datashop.web.ApiException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DownloadSignerTest {

    private final DownloadSigner signer = new DownloadSigner("download-secret");

    @Test
    void verifiesIssuedToken() {
        UUID productId = UUID.randomUUID();
        String token = signer.issue(productId, Duration.ofMinutes(1));

        assertThat(signer.verify(token)).isEqualTo(productId);
    }

    @Test
    void verifiesQueryTicket() {
        UUID productId = UUID.randomUUID();
        DownloadSigner.Ticket ticket = signer.issueTicket(productId, Duration.ofMinutes(1));

        assertThat(signer.verify(ticket.productId(), ticket.expiresAt(), ticket.signature()))
                .isEqualTo(productId);
    }

    @Test
    void rejectsTamperedQueryTicket() {
        DownloadSigner.Ticket ticket = signer.issueTicket(UUID.randomUUID(), Duration.ofMinutes(1));

        assertThatThrownBy(() -> signer.verify(ticket.productId(), ticket.expiresAt(), "bad"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsTamperedToken() {
        String token = signer.issue(UUID.randomUUID(), Duration.ofMinutes(1));

        assertThatThrownBy(() -> signer.verify(token + "x"))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(403);
    }

    @Test
    void rejectsExpiredToken() {
        String token = signer.issue(UUID.randomUUID(), Duration.ofSeconds(-1));

        assertThatThrownBy(() -> signer.verify(token))
                .isInstanceOf(ApiException.class);
    }
}
