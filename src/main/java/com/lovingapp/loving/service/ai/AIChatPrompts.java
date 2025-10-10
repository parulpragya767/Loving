package com.lovingapp.loving.service.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.model.entity.ai.ChatMessage;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LoveType;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing AI chat prompts and context extraction.
 */
@Slf4j
@Component
public class AIChatPrompts {

    /**
     * Generates a system prompt with information about love types and emotional
     * states
     * 
     * @return The system prompt as a string
     * @throws RuntimeException if there's an error reading the prompt file
     */
    public String generateSystemPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/system_prompt.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading system prompt file", e);
            throw new RuntimeException("Failed to load system prompt", e);
        }
    }

    /**
     * Extracts context from the conversation to understand the user's situation
     * 
     * @param conversationHistory The conversation history
     * @param userMessage         The latest user message
     * @param assistantMessage    The assistant's response
     * @return An ExtractionResult containing the extracted context
     */
    public ExtractionResult extractContext(List<ChatMessage> conversationHistory,
            ChatMessage userMessage,
            ChatMessage assistantMessage) {
        UserContextDTO context = new UserContextDTO();
        boolean readyForRecommendation = false;

        // Simple keyword matching for demonstration
        String userMessageLower = userMessage.getContent().toLowerCase();

        // Extract emotional state
        for (EmotionalState state : EmotionalState.values()) {
            if (userMessageLower.contains(state.name().toLowerCase()) ||
                    userMessageLower.contains(state.getDisplayName().toLowerCase())) {
                context.setEmotionalStates(List.of(state));
                break;
            }
        }

        // Extract love language preferences (simplified)
        for (LoveType loveType : LoveType.values()) {
            if (userMessageLower.contains(loveType.name().toLowerCase())) {
                context.setPreferredLoveLanguages(List.of(loveType));
                break;
            }
        }

        // Determine if we have enough context for recommendations
        // This is a simplified condition - you might want to make this more
        // sophisticated
        if (context.getEmotionalStates() != null && !context.getEmotionalStates().isEmpty() &&
                context.getPreferredLoveLanguages() != null && !context.getPreferredLoveLanguages().isEmpty()) {
            readyForRecommendation = true;
        }

        return new ExtractionResult(context, readyForRecommendation);
    }

    /**
     * Inner class to hold the extraction result
     */
    public static class ExtractionResult {
        public final UserContextDTO contextDTO;
        public final boolean readyForRecommendation;

        public ExtractionResult(UserContextDTO contextDTO, boolean readyForRecommendation) {
            this.contextDTO = contextDTO;
            this.readyForRecommendation = readyForRecommendation;
        }

        public UserContextDTO getContextDTO() {
            return contextDTO;
        }

        public boolean isReadyForRecommendation() {
            return readyForRecommendation;
        }
    }
}
