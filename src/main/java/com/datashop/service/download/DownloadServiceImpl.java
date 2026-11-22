package com.datashop.service.download;

import com.datashop.domain.product.Product;
import com.datashop.service.catalog.ProductService;
import com.datashop.service.media.MediaStorage;
import com.datashop.service.order.OrderService;
import com.datashop.util.Constants;
import com.datashop.util.id.Uuids;
import com.datashop.util.web.Errors;
import com.datashop.web.dto.download.DownloadUrlView;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DownloadServiceImpl implements DownloadService {

    private final ProductService productService;
    private final OrderService orderService;
    private final MediaStorage mediaStorage;

    @Override
    public DownloadUrlView issue(UUID userId, String rawProductId) {
        UUID productId = Uuids.parse(rawProductId);
        Product product = productService.requireEntity(productId);
        String publicId = product.getFilePublicId();
        if (publicId == null || publicId.isBlank()
                || !publicId.startsWith(Constants.Media.DATASETS_PREFIX)) {
            throw Errors.notFound(Constants.Messages.DATASET_NOT_AVAILABLE);
        }
        if (!orderService.hasPurchased(userId, productId)) {
            throw Errors.forbidden(Constants.Messages.PURCHASE_REQUIRED);
        }
        String externalUrl = mediaStorage.downloadUrl(
                        product.getFilePublicId(), Constants.Media.DOWNLOAD_LINK_TTL)
                .orElseThrow(() -> new IllegalStateException(
                        "Supabase Storage download URL is unavailable for " + product.getFilePublicId()));
        return DownloadUrlView.builder()
                .url(externalUrl)
                .expiresIn((int) Constants.Media.DOWNLOAD_LINK_TTL.toSeconds())
                .build();
    }
}
