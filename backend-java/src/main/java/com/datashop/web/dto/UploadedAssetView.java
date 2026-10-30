package com.datashop.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UploadedAssetView {

    String publicId;
    String url;
}
