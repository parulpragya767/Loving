package com.lovingapp.model.domain.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.lovingapp.model.enums.Journey;
import com.lovingapp.model.enums.LoveType;
import com.lovingapp.model.enums.RelationalNeed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMEmpatheticResponse {
    @JsonPropertyDescription("The user's current emotional or relational phase in their journey.")
    private List<Journey> inferredJourneys;

    @JsonPropertyDescription("Primary love types expressed or sought by the user.")
    private List<LoveType> inferredLoveTypes;

    @JsonPropertyDescription("Core relational needs present in the user's experience.")
    private List<RelationalNeed> inferredRelationalNeeds;

    @JsonPropertyDescription("Has enum values been successfully inferred for Journey, LoveType and RelationshipNeed?")
    public boolean inferredEnumsForRitualSuggestion;

    @JsonPropertyDescription("Does LLM have all the information to suggest a ritual pack?")
    public boolean readyForRitualSuggestion;

    @JsonPropertyDescription("Empathetic response for the user query")
    public String response;
}
