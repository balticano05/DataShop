package com.datashop.web.api;

import com.datashop.security.RequestAuth;
import com.datashop.service.MediaService;
import io.javalin.Javalin;

public class AdminMediaApiController {

    private final MediaService mediaService;
    private final RequestAuth requestAuth;

    public AdminMediaApiController(MediaService mediaService, RequestAuth requestAuth) {
        this.mediaService = mediaService;
        this.requestAuth = requestAuth;
    }

    public void register(Javalin app) {
        app.post("/api/admin/media/preview", context -> {
            requestAuth.requireAdmin(context);
            context.status(201).json(mediaService.uploadPreview(context.uploadedFile("file")));
        });

        app.post("/api/admin/media/dataset/{productId}", context -> {
            requestAuth.requireAdmin(context);
            context.status(201).json(mediaService.uploadDataset(
                    context.pathParam("productId"), context.uploadedFile("file")));
        });
    }
}
