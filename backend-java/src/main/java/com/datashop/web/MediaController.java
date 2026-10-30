package com.datashop.web;

import com.datashop.service.MediaService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.net.URLConnection;

public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public void register(Javalin app) {
        app.get("/media/images/{filename}", context -> sendImage(context, context.pathParam("filename")));
    }

    private void sendImage(Context context, String filename) {
        byte[] data = mediaService.readPreviewImage(filename);
        String contentType = URLConnection.guessContentTypeFromName(filename);
        context.contentType(contentType == null ? "application/octet-stream" : contentType);
        context.header("Cache-Control", "public, max-age=31536000, immutable");
        context.result(data);
    }
}
