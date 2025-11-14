package com.lovingapp.loving.model.dto;

import java.util.List;

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
        private String key;
        private String displayName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RitualTag {
        private String displayName;
        private List<TagValue> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RitualTags {
        private RitualTag loveTypes;
        private RitualTag ritualModes;
        private RitualTag timeTaken;
        private RitualTag relationalNeeds;
        private RitualTag ritualTones;
    }
}
