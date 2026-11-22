package com.datashop.web.api.download;

import com.datashop.domain.user.AuthUser;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.download.DownloadService;
import com.datashop.util.Constants;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DownloadApiController {

    private final DownloadService downloadService;
    private final RequestAuthenticator requestAuth;

    public void register(Javalin app) {
        app.get(Constants.Routes.API_DOWNLOAD_BY_PRODUCT_ID, context -> {
            AuthUser user = requestAuth.require(context);
            context.json(downloadService.issue(
                    user.getId(), context.pathParam(Constants.Params.PRODUCT_ID)));
        });
    }
}
