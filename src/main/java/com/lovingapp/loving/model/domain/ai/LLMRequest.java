package com.lovingapp.loving.model.domain.ai;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {
    private String systemPrompt;
    private List<LLMChatMessage> messages;
    private LLMResponseFormat responseFormat;
    private Map<String, Object> metadata; // optional — e.g. temperature, schema, etc.
}