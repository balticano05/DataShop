package com.datashop.security.impl;

import com.datashop.config.AppConfig;
import com.datashop.security.SecurityHeaderService;
import com.datashop.util.Constants;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityHeaders implements SecurityHeaderService {

    private final AppConfig config;

    @Override
    public void apply(Context context) {
        context.header(Constants.Security.HEADER_CONTENT_TYPE_OPTIONS, Constants.Security.VALUE_NOSNIFF);
        context.header(Constants.Security.HEADER_FRAME_OPTIONS, Constants.Security.VALUE_DENY);
        context.header(Constants.Security.HEADER_REFERRER_POLICY, Constants.Security.VALUE_STRICT_ORIGIN);
        context.header(
                Constants.Security.HEADER_PERMISSIONS_POLICY, Constants.Security.VALUE_PERMISSIONS_POLICY);
        context.header(
                Constants.Security.HEADER_CONTENT_SECURITY_POLICY,
                isDocsPage(context.path()) ? Constants.Security.CSP_DOCS : Constants.Security.CSP);
        if (config.isSecureCookies()) {
            context.header(
                    Constants.Security.HEADER_STRICT_TRANSPORT_SECURITY, Constants.Security.VALUE_HSTS);
        }
        if (context.path().startsWith(Constants.Http.API_PREFIX)) {
            context.header(Constants.Security.HEADER_CACHE_CONTROL, Constants.Security.VALUE_NO_STORE);
        }
    }

    private boolean isDocsPage(String path) {
        return Constants.Routes.DOCS.equals(path) || (Constants.Routes.DOCS + "/").equals(path);
    }
}
