package com.lovingapp.loving.helpers.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.enums.Journey;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RelationshipStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LLMPromptHelper {
    private static final String USER_CONTEXT_EXTRACTION_PROMPT_FILE_PATH = "prompts/user_context_extraction_prompt.txt";
    private static final String WRAP_UP_CHAT_RESPONSE_PROMPT_FILE_PATH = "prompts/wrap_up_chat_response_prompt.txt";
    private static final String EMPATHETIC_CHAT_RESPONSE_PROMPT_FILE_PATH = "prompts/empathetic_chat_response_prompt.txt";

    public static String generateEmpatheticChatResponsePrompt() {
        String template = readPromptFile(EMPATHETIC_CHAT_RESPONSE_PROMPT_FILE_PATH);

        String loveTypeDefs = enumList(LoveType.values());
        String journeyDefs = enumList(Journey.values());
        String relationalNeedsDefs = enumList(RelationalNeed.values());
        String relationshipStatusDefs = enumList(RelationshipStatus.values());

        return template
                .replace("{{LOVE_TYPES_ENUM}}", loveTypeDefs)
                .replace("{{JOURNEY_ENUM}}", journeyDefs)
                .replace("{{RELATIONAL_NEEDS_ENUM}}", relationalNeedsDefs)
                .replace("{{RELATIONSHIP_STATUS_ENUM}}", relationshipStatusDefs);
    }

    public static String generateUserContextExtractionPrompt() {
        String template = readPromptFile(USER_CONTEXT_EXTRACTION_PROMPT_FILE_PATH);

        String loveTypeDefs = enumList(LoveType.values());
        String journeyDefs = enumList(Journey.values());
        String relationalNeedsDefs = enumList(RelationalNeed.values());
        String relationshipStatusDefs = enumList(RelationshipStatus.values());

        return template
                .replace("{{LOVE_TYPES_ENUM}}", loveTypeDefs)
                .replace("{{JOURNEY_ENUM}}", journeyDefs)
                .replace("{{RELATIONAL_NEEDS_ENUM}}", relationalNeedsDefs)
                .replace("{{RELATIONSHIP_STATUS_ENUM}}", relationshipStatusDefs);
    }

    public static String generateWrapUpChatResponsePrompt(RitualPackDTO ritualPack) {
        String template = readPromptFile(WRAP_UP_CHAT_RESPONSE_PROMPT_FILE_PATH);

        String loveTypeDefs = enumList(LoveType.values());

        // Format the ritual pack details
        StringBuilder packDetails = new StringBuilder();
        if (ritualPack != null) {
            if (ritualPack.getTitle() != null) {
                packDetails.append("Title: ").append(ritualPack.getTitle()).append("\n");
            }
            if (ritualPack.getDescription() != null) {
                packDetails.append("Description: ").append(ritualPack.getDescription()).append("\n");
            }
            if (ritualPack.getHowItHelps() != null) {
                packDetails.append("How It Helps: ").append(ritualPack.getHowItHelps());
            }
        }

        return template
                .replace("{{LOVE_TYPES_ENUM}}", loveTypeDefs)
                .replace("{{SUGGESTED_RITUAL_PACK}}", packDetails.toString());
    }

    private static String enumList(Enum<?>[] values) {
        return Arrays.stream(values)
                .map(e -> {
                    try {
                        Object desc = e.getClass().getMethod("getDescription").invoke(e);
                        if (desc != null) {
                            return e.name() + " → " + desc.toString();
                        }
                    } catch (Exception ignored) {
                        // No getDescription method or invocation failed; fall back to name
                    }
                    return e.name();
                })
                .collect(Collectors.joining("\n"));
    }

    private static String readPromptFile(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading system prompt file", e);
            throw new RuntimeException("Failed to load system prompt", e);
        }
    }
}
