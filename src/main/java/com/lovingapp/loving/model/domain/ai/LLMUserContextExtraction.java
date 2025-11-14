package com.lovingapp.loving.model.domain.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.lovingapp.loving.model.enums.Journey;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RelationshipStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMUserContextExtraction {
    @JsonPropertyDescription("The user's current emotional or relational phase in their journey. Must map to one enum value from Journey.")
    private Journey journey;

    @JsonPropertyDescription("The primary love types being expressed or sought by the user — e.g., CARE, FIRE, BELONG. Choose 1–3 values that best represent their emotional theme.")
    private List<LoveType> loveTypes;

    @JsonPropertyDescription("The relational or emotional needs most present in the user's current experience — e.g., CONNECTION, UNDERSTANDING, TRUST_AND_SAFETY. Choose 1–3 values that fit best.")
    private List<RelationalNeed> relationalNeeds;

    @JsonPropertyDescription("The user’s relationship status if clearly implied. If uncertain or not expressed, return null.")
    private RelationshipStatus relationshipStatus;

    @JsonPropertyDescription("A concise, empathetic 2–4 sentence summary describing the user’s emotional state, what they’re experiencing in their relationship, and what they seem to long for or need.")
    private String semanticSummary;

    @JsonPropertyDescription("A concise, emotionally aligned conversation title based on the core theme of the conversation so far.")
    private String conversationTitle;
}
