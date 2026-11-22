package com.datashop.web.dto.media;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public final class UploadedAssetView {

    private final String publicId;
    private final String url;
}
