package com.lovingapp.loving.helpers.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.lovingapp.loving.model.dto.UserContextDTO;
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

@Component
public class UserContextExtractor {

    public UserContextDTO parseAndValidate(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Extraction JSON is missing or not an object");
        }

        UserContextDTO dto = new UserContextDTO();

        // Scalars
        dto.setAvailableTimeMinutes(
                root.path("availableTimeMinutes").isNumber() ? root.get("availableTimeMinutes").asInt() : null);
        dto.setPreferredEffortLevel(parseEnumOrNull(root.path("preferredEffortLevel").asText(null), EffortLevel.class));
        dto.setPreferredIntensity(parseEnumOrNull(root.path("preferredIntensity").asText(null), IntensityLevel.class));
        dto.setTimeContext(parseEnumOrNull(root.path("timeContext").asText(null), TimeContext.class));
        dto.setRelationshipStatus(
                parseEnumOrNull(root.path("relationshipStatus").asText(null), RelationshipStatus.class));
        dto.setSemanticQuery(root.path("semanticQuery").asText(null));

        // Arrays (deduped, max 3)
        dto.setEmotionalStates(parseEnumArray(root.get("emotionalStates"), EmotionalState.class));
        dto.setRelationalNeeds(parseEnumArray(root.get("relationalNeeds"), RelationalNeed.class));
        dto.setPreferredLoveLanguages(parseEnumArray(root.get("preferredLoveLanguages"), LoveType.class));
        dto.setPreferredRitualTypes(parseEnumArray(root.get("preferredRitualTypes"), RitualType.class));
        dto.setPreferredTones(parseEnumArray(root.get("preferredTones"), RitualTone.class));
        dto.setCurrentContexts(parseEnumArray(root.get("currentContexts"), LifeContext.class));

        return dto;
    }

    private <E extends Enum<E>> E parseEnumOrNull(String name, Class<E> type) {
        if (name == null || name.isBlank() || "null".equalsIgnoreCase(name))
            return null;
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid enum value '" + name + "' for " + type.getSimpleName());
        }
    }

    private <E extends Enum<E>> List<E> parseEnumArray(JsonNode node, Class<E> type) {
        List<E> out = new ArrayList<>();
        if (node == null || node.isNull())
            return out;
        if (!node.isArray())
            throw new IllegalArgumentException("Expected array for " + type.getSimpleName());
        Set<String> seen = new LinkedHashSet<>();
        for (JsonNode n : (ArrayNode) node) {
            if (!n.isTextual())
                continue;
            String name = n.asText();
            if (name == null || name.isBlank())
                continue;
            if (seen.contains(name))
                continue;
            out.add(parseEnumOrNull(name, type));
            seen.add(name);
            if (out.size() == 3)
                break; // cap at 3
        }
        return out;
    }
}
