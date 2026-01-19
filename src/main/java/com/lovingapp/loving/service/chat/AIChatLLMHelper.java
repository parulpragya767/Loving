package com.lovingapp.loving.service.chat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.client.LlmClient;
import com.lovingapp.loving.helpers.ai.LLMPromptHelper;
import com.lovingapp.loving.model.domain.ai.LLMChatMessage;
import com.lovingapp.loving.model.domain.ai.LLMEmpatheticResponse;
import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;
import com.lovingapp.loving.model.domain.ai.LLMResponseFormat;
import com.lovingapp.loving.model.domain.ai.LLMUserContextExtraction;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for LLM-related operations in AI chat service.
 * Handles request creation, LLM calls, and response generation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIChatLLMHelper {

    private final LlmClient llmClient;

    /**
     * Generate empathetic response from conversation using LLM.
     */
    public LLMEmpatheticResponse generateEmpatheticResponse(UUID sessionId, List<ChatMessage> messages) {
        LLMRequest llmRequest = LLMRequest.builder()
                .messages(messages.stream()
                        .map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
                        .collect(Collectors.toList()))
                .systemPrompt(LLMPromptHelper.generateEmpatheticChatResponsePrompt())
                .responseFormat(LLMResponseFormat.JSON)
                .build();

        // Call LLM to generate empathetic response
        log.info("Generating empathetic response via LLM sessionId={}", sessionId);

        LLMResponse<LLMEmpatheticResponse> aiReply = llmClient.generate(llmRequest, LLMEmpatheticResponse.class);
        LLMEmpatheticResponse empatheticResponse = aiReply.getParsed();

        log.info("Empathetic response via LLM generated successfully sessionId={}", sessionId);

        return empatheticResponse;
    }

    /**
     * Extract user context from conversation using LLM.
     */
    public LLMUserContextExtraction extractUserContext(UUID userId, UUID sessionId, List<ChatMessage> messages) {
        LLMRequest extractionRequest = LLMRequest.builder()
                .messages(messages.stream()
                        .map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
                        .collect(Collectors.toList()))
                .systemPrompt(LLMPromptHelper.generateUserContextExtractionPrompt())
                .responseFormat(LLMResponseFormat.JSON)
                .build();

        log.info("Extracting user context from conversation via LLM sessionId={}", sessionId);

        LLMResponse<LLMUserContextExtraction> llmUserContextResponse = llmClient.generate(extractionRequest,
                LLMUserContextExtraction.class);
        LLMUserContextExtraction llmUserContext = llmUserContextResponse.getParsed();

        log.info("User context extracted successfully via LLM sessionId={}", sessionId);

        return llmUserContext;
    }

    /**
     * Generate contextual wrap-up message via LLM that ties the pack to the user's
     * situation.
     */
    public String generateWrapUpMessage(List<ChatMessage> messages, RitualPackDTO recommendedPack, UUID sessionId) {
        String wrapUpMessage = "";

        try {
            if (recommendedPack != null) {
                LLMRequest wrapUpRequest = LLMRequest.builder()
                        .messages(messages.stream()
                                .map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
                                .collect(Collectors.toList()))
                        .systemPrompt(LLMPromptHelper.generateWrapUpChatResponsePrompt(recommendedPack))
                        .responseFormat(LLMResponseFormat.TEXT)
                        .build();

                log.info("Generating contextual wrap-up message via LLM sessionId={}", sessionId);

                LLMResponse<String> wrapUpResponse = llmClient.generate(wrapUpRequest);
                wrapUpMessage = wrapUpResponse.getRawText();

                log.info("Contextual wrap-up message generated successfully sessionId={}", sessionId);
            }

            if (wrapUpMessage == null || wrapUpMessage.trim().isEmpty()) {
                wrapUpMessage = getFallbackWrapUpMessage(recommendedPack);
            }
        } catch (Exception ex) {
            log.warn("Ritual wrap-up message LLM generation failed sessionId={}: {}", sessionId, ex.getMessage());
            wrapUpMessage = getFallbackWrapUpMessage(recommendedPack);
        }

        return wrapUpMessage;
    }

    /**
     * Get fallback wrap-up messages in case the LLM call fails.
     */
    private String getFallbackWrapUpMessage(RitualPackDTO recommendedPack) {
        String wrapUpMessage;

        if (recommendedPack != null) {
            wrapUpMessage = String.format("I recommend the '%s' ritual pack for you! %s",
                    recommendedPack.getTitle(),
                    recommendedPack.getTagLine() != null ? recommendedPack.getTagLine() : "");
        } else {
            wrapUpMessage = "I've analyzed your conversation. Here's a ritual pack that might interest you.";
        }

        return wrapUpMessage;
    }
}
