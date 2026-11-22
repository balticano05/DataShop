package com.datashop.web.api.media;

import com.datashop.security.RequestAuthenticator;
import com.datashop.service.media.MediaService;
import com.datashop.util.Constants;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdminMediaApiController {

    private final MediaService mediaService;
    private final RequestAuthenticator requestAuth;

    public void register(Javalin app) {
        app.post(Constants.Routes.API_ADMIN_MEDIA_PREVIEW, context -> {
            requestAuth.requireAdmin(context);
            context.status(Constants.Http.Status.CREATED).json(mediaService.uploadPreview(
                    context.uploadedFile(Constants.Params.FILE)));
        });

        app.post(Constants.Routes.API_ADMIN_MEDIA_DATASET, context -> {
            requestAuth.requireAdmin(context);
            context.status(Constants.Http.Status.CREATED).json(mediaService.uploadDataset(
                    context.pathParam(Constants.Params.PRODUCT_ID),
                    context.uploadedFile(Constants.Params.FILE)));
        });
    }
}
