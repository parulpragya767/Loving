package com.lovingapp.loving.helpers.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RelationshipStatus;

@Component
public class UserContextExtractor {

    public UserContextDTO parseAndValidate(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Extraction JSON is missing or not an object");
        }

        UserContextDTO dto = new UserContextDTO();

        // Scalars (supported)
        dto.setRelationshipStatus(
                parseEnumOrNull(root.path("relationshipStatus").asText(null), RelationshipStatus.class));
        dto.setSemanticSummary(root.path("semanticSummary").asText(null));

        // Arrays (deduped, max 3 where applicable)
        dto.setRelationalNeeds(parseEnumArray(root.get("relationalNeeds"), RelationalNeed.class));
        dto.setLoveTypes(parseEnumArray(root.get("loveTypes"), LoveType.class));

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
