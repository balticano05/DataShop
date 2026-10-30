package com.datashop.service;

import com.datashop.config.AppConfig;
import com.datashop.domain.Product;
import com.datashop.security.DownloadSigner;
import com.datashop.util.Uuids;
import com.datashop.web.Errors;
import com.datashop.web.dto.DownloadUrlView;
import lombok.RequiredArgsConstructor;

import java.net.URLConnection;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
public class DownloadService {

    private static final Duration LINK_TTL = Duration.ofSeconds(60);

    private final ProductService productService;
    private final OrderService orderService;
    private final MediaStorage mediaStorage;
    private final DownloadSigner downloadSigner;
    private final AppConfig config;

    public DownloadUrlView issue(UUID userId, String rawProductId) {
        UUID productId = Uuids.parse(rawProductId);
        Product product = productService.requireEntity(productId);
        if (product.getFilePublicId() == null || product.getFilePublicId().isBlank()) {
            throw Errors.notFound("Dataset file is not available");
        }
        if (!orderService.hasPurchased(userId, productId)) {
            throw Errors.forbidden("Purchase is required to download this dataset");
        }
        String externalUrl = mediaStorage.downloadUrl(product.getFilePublicId(), LINK_TTL);
        if (externalUrl != null) {
            return DownloadUrlView.builder()
                    .url(externalUrl)
                    .expiresIn((int) LINK_TTL.toSeconds())
                    .build();
        }
        DownloadSigner.Ticket ticket = downloadSigner.issueTicket(productId, LINK_TTL);
        String url = config.getPublicBaseUrl() + "/downloads/file"
                + "?productId=" + productId
                + "&expires_at=" + ticket.expiresAt()
                + "&signature=" + ticket.signature();
        return DownloadUrlView.builder()
                .url(url)
                .expiresIn((int) LINK_TTL.toSeconds())
                .build();
    }

    public DownloadedFile download(String rawProductId, String rawExpiresAt, String signature) {
        UUID productId = Uuids.parseOrNull(rawProductId);
        long expiresAt;
        try {
            expiresAt = Long.parseLong(rawExpiresAt == null ? "" : rawExpiresAt.trim());
        } catch (NumberFormatException exception) {
            throw Errors.forbidden("Invalid or expired download link");
        }
        return load(downloadSigner.verify(productId, expiresAt, signature));
    }

    public DownloadedFile download(String token) {
        return load(downloadSigner.verify(token));
    }

    private DownloadedFile load(UUID productId) {
        Product product = productService.requireEntity(productId);
        if (product.getFilePublicId() == null || product.getFilePublicId().isBlank()) {
            throw Errors.notFound("Dataset file is not available");
        }
        byte[] data = mediaStorage.read(product.getFilePublicId());
        String storedName = product.getFilePublicId()
                .substring(product.getFilePublicId().lastIndexOf('/') + 1)
                .replaceFirst("^[0-9a-fA-F-]{36}-", "");
        String contentType = URLConnection.guessContentTypeFromName(storedName);
        return new DownloadedFile(data, storedName, contentType == null
                ? "application/octet-stream"
                : contentType);
    }

    public record DownloadedFile(byte[] data, String filename, String contentType) {
    }
}
