package com.lovingapp.loving.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAsset {
    private String id;
    private String type; // e.g., "image", "audio"
    private String url;
}
