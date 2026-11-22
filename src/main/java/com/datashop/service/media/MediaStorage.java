package com.datashop.service.media;

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

public interface MediaStorage {

    StoredMedia store(InputStream data, String originalFilename, String contentType);

    void delete(String publicId);

    Optional<String> downloadUrl(String publicId, Duration ttl);

    record StoredMedia(String publicId) {
    }
}
