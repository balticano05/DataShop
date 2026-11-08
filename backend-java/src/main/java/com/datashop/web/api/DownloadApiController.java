package com.datashop.web.api;

import com.datashop.security.AuthUser;
import com.datashop.security.RequestAuth;
import com.datashop.service.DownloadService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class DownloadApiController {

    private final DownloadService downloadService;
    private final RequestAuth requestAuth;

    public DownloadApiController(DownloadService downloadService, RequestAuth requestAuth) {
        this.downloadService = downloadService;
        this.requestAuth = requestAuth;
    }

    public void register(Javalin app) {
        app.get("/api/downloads/{productId}", context -> {
            AuthUser user = requestAuth.require(context);
            context.json(downloadService.issue(user.getId(), context.pathParam("productId")));
        });

        app.get("/downloads/file", context -> {
            DownloadService.DownloadedFile file = downloadService.download(
                    context.queryParam("productId"),
                    context.queryParam("expires_at"),
                    context.queryParam("signature"));
            send(context, file);
        });

        app.get("/downloads/{token}", context -> {
            DownloadService.DownloadedFile file = downloadService.download(context.pathParam("token"));
            send(context, file);
        });
    }

    private static void send(Context context, DownloadService.DownloadedFile file) {
        context.header("Content-Disposition",
                "attachment; filename=\"" + file.filename().replace("\"", "") + "\"");
        context.header("Cache-Control", "no-store");
        context.contentType(file.contentType());
        context.result(file.data());
    }
}
