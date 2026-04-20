package com.lovingapp.model.domain.ai;

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
    private String model;
    private String systemPrompt;
    private List<LLMChatMessage> messages;
    private LLMResponseFormat responseFormat;
    private Map<String, Object> metadata;

    private String promptId;
    private String promptVersion;
    private String promptName;
    private Map<String, String> promptVariables;
}