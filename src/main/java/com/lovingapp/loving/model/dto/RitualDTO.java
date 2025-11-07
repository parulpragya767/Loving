package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.domain.MediaAsset;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.PublicationStatus;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RitualMode;
import com.lovingapp.loving.model.enums.RitualTone;
import com.lovingapp.loving.model.enums.TimeTaken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RitualDTO {
    private UUID id;
    private String title;
    private String description;
    private RitualMode ritualMode;
    private List<RitualTone> ritualTones;
    private TimeTaken timeTaken;
    private List<String> steps;
    private List<MediaAsset> mediaAssets;
    private List<LoveType> loveTypes;
    private List<RelationalNeed> relationalNeeds;
    private String semanticSummary;
    private PublicationStatus status;
    private String contentHash;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
