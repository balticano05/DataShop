package com.datashop.repository;

import com.datashop.domain.media.MediaAsset;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository {

    MediaAsset save(EntityManager entityManager, MediaAsset asset);

    Optional<MediaAsset> findById(EntityManager entityManager, UUID id);
}
