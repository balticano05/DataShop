package com.datashop.web.dto.download;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public final class DownloadUrlView {

    private final String url;
    private final int expiresIn;
}
