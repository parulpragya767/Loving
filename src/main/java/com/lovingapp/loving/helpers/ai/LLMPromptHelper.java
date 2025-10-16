package com.lovingapp.loving.helpers.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.LoveTypeInfo;
import com.lovingapp.loving.model.enums.EffortLevel;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.IntensityLevel;
import com.lovingapp.loving.model.enums.LifeContext;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RelationshipStatus;
import com.lovingapp.loving.model.enums.RitualTone;
import com.lovingapp.loving.model.enums.RitualType;
import com.lovingapp.loving.model.enums.TimeContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LLMPromptHelper {
    private final String empatheticChatResponsePromptFilePath = "prompts/empathetic_chat_response_prompt.txt";
    private final String userContextExtractionPromptFilePath = "prompts/user_context_extraction_prompt.txt";
    private final String ritualWrapUpPromptFilePath = "prompts/ritual_wrap_up_prompt.txt";
    private final String sampleChatPromptsPromptFilePath = "prompts/sample_chat_prompts_prompt.txt";

    /**
     * Generate the empathetic chat system prompt, formatting and injecting the
     * provided love type definitions into the template.
     */
    public String generateEmpatheticChatResponsePrompt(List<LoveTypeInfo> loveTypes) {
        String template = readPromptFile(empatheticChatResponsePromptFilePath);
        String withEnums = injectDynamicDomainKnowledge(template);
        String loveTypeDefs = loveTypes == null || loveTypes.isEmpty()
                ? ""
                : formatLoveTypeDefinitions(loveTypes);
        return withEnums.replace("{{LOVE_TYPES_DEFINITIONS}}", loveTypeDefs);
    }

    /**
     * Format a list of LoveTypeInfo objects into a string representation.
     * Each line follows the format: "NAME → Title — Subtitle (or description
     * snippet)"
     */
    private String formatLoveTypeDefinitions(List<LoveTypeInfo> loveTypes) {
        return loveTypes.stream()
                .sorted((a, b) -> a.getLoveType().name().compareTo(b.getLoveType().name()))
                .map(this::formatLoveTypeDefinition)
                .collect(Collectors.joining("\n"));
    }

    private String formatLoveTypeDefinition(LoveTypeInfo info) {
        // Format: NAME → Title. Optional subtitle. If no title, fallback to description
        // snippet.
        String name = info.getLoveType() != null ? info.getLoveType().name() : "";
        String title = info.getTitle();
        String subtitle = info.getSubtitle();
        String description = info.getDescription();

        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" → ");
        if (title != null && !title.isBlank()) {
            sb.append(title);
            if (subtitle != null && !subtitle.isBlank()) {
                sb.append(" — ").append(subtitle);
            }
        } else if (description != null && !description.isBlank()) {
            String desc = description.replaceAll("\n", " ").trim();
            if (desc.length() > 140) {
                desc = desc.substring(0, 137) + "...";
            }
            sb.append(desc);
        } else {
            sb.append("No description available");
        }
        return sb.toString();
    }

    private String readPromptFile(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading system prompt file", e);
            throw new RuntimeException("Failed to load system prompt", e);
        }
    }

    private String injectDynamicDomainKnowledge(String content) {
        try {
            String emotionalStates = enumList(EmotionalState.values());
            String relationalNeeds = enumList(RelationalNeed.values());
            String lifeContexts = enumList(LifeContext.values());

            return content
                    .replace("{{EMOTIONAL_STATES_ENUM}}", emotionalStates)
                    .replace("{{RELATIONAL_NEEDS_ENUM}}", relationalNeeds)
                    .replace("{{LIFE_CONTEXTS_ENUM}}", lifeContexts);
            // Note: {{LOVE_TYPES_DEFINITIONS}} intentionally left for future injection from
            // a dedicated resource
        } catch (Exception ex) {
            log.warn("Failed to inject dynamic domain knowledge into prompt. Falling back to template.", ex);
            return content;
        }
    }

    private String enumList(Enum<?>[] values) {
        return Arrays.stream(values)
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Generate the user context extraction system prompt by injecting all enum NAME
     * lists and optional enum definitions. The full chat history will be supplied
     * to the LLM as separate messages; this method only prepares the system
     * instructions.
     */
    public String generateUserContextExtractionPrompt(String optionalEnumDefinitions) {
        String template = readPromptFile(userContextExtractionPromptFilePath);

        String content = template
                .replace("{{EMOTIONAL_STATES_ENUM_NAMES}}", enumList(EmotionalState.values()))
                .replace("{{RELATIONAL_NEEDS_ENUM_NAMES}}", enumList(RelationalNeed.values()))
                .replace("{{LOVE_TYPES_ENUM_NAMES}}", enumList(LoveType.values()))
                .replace("{{RITUAL_TYPES_ENUM_NAMES}}", enumList(RitualType.values()))
                .replace("{{RITUAL_TONES_ENUM_NAMES}}", enumList(RitualTone.values()))
                .replace("{{LIFE_CONTEXTS_ENUM_NAMES}}", enumList(LifeContext.values()))
                .replace("{{TIME_CONTEXT_ENUM_NAMES}}", enumList(TimeContext.values()))
                .replace("{{RELATIONSHIP_STATUS_ENUM_NAMES}}", enumList(RelationshipStatus.values()))
                .replace("{{EFFORT_LEVEL_ENUM_NAMES}}", enumList(EffortLevel.values()))
                .replace("{{INTENSITY_LEVEL_ENUM_NAMES}}", enumList(IntensityLevel.values()))
                .replace("{{OPTIONAL_ENUM_DEFINITIONS}}",
                        optionalEnumDefinitions == null ? "" : optionalEnumDefinitions);

        return content;
    }

    /**
     * Generate the ritual wrap-up system prompt that ties the recommended ritual
     * pack
     * to the user's situation using internal domain knowledge without exposing enum
     * labels.
     */
    public String generateRitualWrapUpPrompt(List<LoveTypeInfo> loveTypes, RitualPackDTO pack) {
        String template = readPromptFile(ritualWrapUpPromptFilePath);
        String withEnums = injectDynamicDomainKnowledge(template);

        String loveTypeDefs = (loveTypes == null || loveTypes.isEmpty())
                ? ""
                : formatLoveTypeDefinitions(loveTypes);

        String packTitle = pack != null && pack.getTitle() != null ? pack.getTitle() : "";
        String packShort = pack != null && pack.getShortDescription() != null ? pack.getShortDescription() : "";
        String packTags = pack != null ? formatPackTags(pack) : "";

        return withEnums
                .replace("{{LOVE_TYPES_DEFINITIONS}}", loveTypeDefs)
                .replace("{{PACK_TITLE}}", escapeCurly(packTitle))
                .replace("{{PACK_SHORT_DESCRIPTION}}", escapeCurly(packShort))
                .replace("{{PACK_TAGS}}", escapeCurly(packTags));
    }

    public String generateSamplePromptsPrompt(String recentContext) {
        String template = readPromptFile(sampleChatPromptsPromptFilePath);
        return template
                .replace("{{RECENT_CONTEXT}}", recentContext);
    }

    private String formatPackTags(RitualPackDTO pack) {
        StringBuilder sb = new StringBuilder();
        // Love types
        if (pack.getLoveTypesSupported() != null && !pack.getLoveTypesSupported().isEmpty()) {
            sb.append("loveTypes=")
                    .append(pack.getLoveTypesSupported().stream().map(Enum::name).collect(Collectors.joining("|")));
        }
        // Relational needs
        if (pack.getRelationalNeedsServed() != null && !pack.getRelationalNeedsServed().isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append("relationalNeeds=")
                    .append(pack.getRelationalNeedsServed().stream().map(Enum::name).collect(Collectors.joining("|")));
        }
        // Life contexts
        if (pack.getLifeContextsRelevant() != null && !pack.getLifeContextsRelevant().isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append("lifeContexts=")
                    .append(pack.getLifeContextsRelevant().stream().map(Enum::name).collect(Collectors.joining("|")));
        }
        // Emotional states
        if (pack.getEmotionalStatesSupported() != null && !pack.getEmotionalStatesSupported().isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append("emotionalStates=").append(
                    pack.getEmotionalStatesSupported().stream().map(Enum::name).collect(Collectors.joining("|")));
        }
        return sb.toString();
    }

    private String escapeCurly(String input) {
        // Avoid accidental template collisions if curly braces appear in content
        if (input == null)
            return "";
        return input.replace("{{", "{").replace("}}", "}");
    }
}
