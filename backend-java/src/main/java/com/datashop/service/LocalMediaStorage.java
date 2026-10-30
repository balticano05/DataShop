package com.datashop.service;

import com.datashop.config.AppConfig;
import com.datashop.web.Errors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

public class LocalMediaStorage implements MediaStorage {

    private final Path root;
    private final String publicBaseUrl;

    public LocalMediaStorage(AppConfig config) {
        this.root = Path.of(config.getUploadsDirectory()).toAbsolutePath().normalize();
        this.publicBaseUrl = config.getPublicBaseUrl();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot create uploads directory " + root, exception);
        }
    }

    @Override
    public StoredMedia store(InputStream data, String originalFilename, String contentType, String folder) {
        String safeFolder = sanitizeFolder(folder);
        String filename = UUID.randomUUID() + "-" + sanitizeFilename(originalFilename);
        String publicId = safeFolder + "/" + filename;
        Path target = resolve(publicId);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            long size = Files.size(target);
            return new StoredMedia(publicId, publicBaseUrl + "/media/" + publicId, contentType, size);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot store uploaded file", exception);
        }
    }

    @Override
    public byte[] read(String publicId) {
        Path path = resolve(publicId);
        if (!Files.isRegularFile(path)) {
            throw Errors.notFound("File not found");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read stored file", exception);
        }
    }

    @Override
    public boolean exists(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return false;
        }
        return Files.isRegularFile(resolve(publicId));
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(publicId));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot delete stored file " + publicId, exception);
        }
    }

    private Path resolve(String publicId) {
        Path candidate = root.resolve(publicId).normalize();
        if (!candidate.startsWith(root)) {
            throw Errors.badRequest("Invalid file path");
        }
        return candidate;
    }

    private String sanitizeFolder(String folder) {
        String normalized = folder == null ? "misc" : folder.replaceAll("[^a-zA-Z0-9_-]", "");
        return normalized.isEmpty() ? "misc" : normalized.toLowerCase(Locale.ROOT);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        String clean = Path.of(filename).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return clean.length() > 80 ? clean.substring(clean.length() - 80) : clean;
    }
}
