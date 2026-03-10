package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum FeatureType {
    AI_CHAT,
    AI_RECOMMENDATION
}
