package com.lovingapp.loving.model.domain.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMEmpatheticResponse {
    @JsonPropertyDescription("Empathetic response for the user query")
    public String response;

    @JsonPropertyDescription("Does LLM have all the information to suggest a ritual pack?")
    public boolean readyForRitualSuggestion;
}
