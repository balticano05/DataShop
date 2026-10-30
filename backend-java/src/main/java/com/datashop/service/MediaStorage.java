package com.datashop.service;

import java.io.InputStream;
import java.time.Duration;

public interface MediaStorage {

    StoredMedia store(InputStream data, String originalFilename, String contentType, String folder);

    byte[] read(String publicId);

    boolean exists(String publicId);

    void delete(String publicId);

    default String downloadUrl(String publicId, Duration ttl) {
        return null;
    }

    record StoredMedia(String publicId, String url, String contentType, long size) {
    }
}
