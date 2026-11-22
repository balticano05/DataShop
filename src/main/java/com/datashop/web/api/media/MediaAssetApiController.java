package com.datashop.web.api.media;

import com.datashop.service.media.MediaImage;
import com.datashop.service.media.MediaService;
import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MediaAssetApiController {

    private final MediaService mediaService;

    public void register(Javalin app) {
        app.get(Constants.Routes.MEDIA_ASSET, context -> {
            UUID id;
            try {
                id = UUID.fromString(context.pathParam(Constants.Params.ID));
            } catch (IllegalArgumentException exception) {
                throw Errors.notFound(Constants.Messages.IMAGE_NOT_FOUND);
            }
            MediaImage image = mediaService.loadImage(id)
                    .orElseThrow(() -> Errors.notFound(Constants.Messages.IMAGE_NOT_FOUND));
            context.header(
                    Constants.Http.CACHE_CONTROL, Constants.Media.ASSET_CACHE_CONTROL);
            context.contentType(image.contentType());
            context.result(image.data());
        });
    }
}
