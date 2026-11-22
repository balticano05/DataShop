package com.datashop.service.media;

/**
 * Binary image data loaded from Supabase PostgreSQL.
 */
public record MediaImage(String contentType, byte[] data) {
}
