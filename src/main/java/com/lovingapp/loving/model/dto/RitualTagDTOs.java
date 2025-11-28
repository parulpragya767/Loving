package com.lovingapp.loving.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for Ritual Tags.
 */
public class RitualTagDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagValue {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String key;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String displayName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RitualTag {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String displayName;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private List<TagValue> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RitualTags {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualTag loveTypes;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualTag ritualModes;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualTag timeTaken;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualTag relationalNeeds;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualTag ritualTones;
    }
}
