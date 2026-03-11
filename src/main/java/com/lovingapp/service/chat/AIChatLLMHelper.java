package com.lovingapp.service.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.lovingapp.client.LlmClient;
import com.lovingapp.config.llm.PromptConfig;
import com.lovingapp.config.llm.PromptConfigConstants;
import com.lovingapp.model.domain.ai.LLMEmpatheticResponse;
import com.lovingapp.model.domain.ai.LLMRequest;
import com.lovingapp.model.domain.ai.LLMResponse;
import com.lovingapp.model.domain.ai.LLMResponseFormat;
import com.lovingapp.model.domain.ai.LLMUserContextExtraction;
import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.entity.ChatMessage;
import com.lovingapp.model.enums.ChatMessageRole;
import com.lovingapp.model.enums.LoveType;

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
        PromptConfig promptConfig = PromptConfigConstants.EMPATHETIC_CHAT_RESPONSE;

        // Get all messages except the last one for conversation context
        String conversationText = createConversationText(messages.subList(0, messages.size() - 1));

        Map<String, String> variables = new HashMap<>();
        variables.put(PromptConfigConstants.CONVERSATION_VARIABLE, conversationText);
        variables.put(PromptConfigConstants.LATEST_USER_MESSAGE_VARIABLE, messages.getLast().getContent());

        LLMRequest llmRequest = LLMRequest.builder()
                .promptId(promptConfig.getPromptId())
                .promptVersion(promptConfig.getPromptVersion())
                .promptVariables(variables)
                .responseFormat(LLMResponseFormat.JSON)
                .build();

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
        PromptConfig promptConfig = PromptConfigConstants.USER_CONTEXT_EXTRACTION;

        Map<String, String> variables = new HashMap<>();
        variables.put(PromptConfigConstants.CONVERSATION_VARIABLE, createConversationText(messages));

        LLMRequest extractionRequest = LLMRequest.builder()
                .promptId(promptConfig.getPromptId())
                .promptVersion(promptConfig.getPromptVersion())
                .promptVariables(variables)
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
                PromptConfig promptConfig = PromptConfigConstants.WRAP_UP_CHAT_RESPONSE;

                Map<String, String> variables = new HashMap<>();
                variables.put(PromptConfigConstants.CONVERSATION_VARIABLE, createConversationText(messages));
                variables.put(PromptConfigConstants.RECOMMENDED_RITUAL_PACK_VARIABLE,
                        ritualPackToPromptString(recommendedPack));

                LLMRequest wrapUpRequest = LLMRequest.builder()
                        .promptId(promptConfig.getPromptId())
                        .promptVersion(promptConfig.getPromptVersion())
                        .promptVariables(variables)
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

    /**
     * Create conversation text from messages with proper formatting.
     */
    private String createConversationText(List<ChatMessage> messages) {
        String conversationText = messages.stream()
                .map(m -> {
                    String roleLabel = m.getRole() == ChatMessageRole.USER ? "[User]" : "[Assistant]";
                    return roleLabel + "\n" + m.getContent();
                })
                .collect(Collectors.joining("\n\n"));

        return conversationText;
    }

    public static String ritualPackToPromptString(RitualPackDTO pack) {
        return """
                Title: %s
                Tagline: %s

                Summary:
                %s

                How It Helps:
                %s

                Love Types: %s
                """.formatted(
                pack.getTitle(),
                pack.getTagLine(),
                pack.getDescription(),
                pack.getHowItHelps(),
                String.join(", ", pack.getLoveTypes().stream().map(LoveType::name).toList()));
    }
}
