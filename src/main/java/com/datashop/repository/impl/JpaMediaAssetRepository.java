package com.datashop.repository.impl;

import com.datashop.domain.media.MediaAsset;
import com.datashop.repository.MediaAssetRepository;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public class JpaMediaAssetRepository implements MediaAssetRepository {

    @Override
    public MediaAsset save(EntityManager entityManager, MediaAsset asset) {
        entityManager.persist(asset);
        entityManager.flush();
        return asset;
    }

    @Override
    public Optional<MediaAsset> findById(EntityManager entityManager, UUID id) {
        return Optional.ofNullable(entityManager.find(MediaAsset.class, id));
    }
}
