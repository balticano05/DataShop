package com.datashop.service.media;

import com.datashop.domain.product.Product;
import jakarta.persistence.EntityManager;

public interface ProductPreviewService {

    void ensurePreviews();

    void generatePreviews(EntityManager entityManager, Product product);
}
