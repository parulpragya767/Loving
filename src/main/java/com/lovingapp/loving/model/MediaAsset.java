package com.lovingapp.loving.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAsset {
    private String id;
    private String type; // e.g., "image", "audio"
    private String url;
    private Ritual ritual;
}
