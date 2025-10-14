package com.lovingapp.loving.helpers.ai;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.model.domain.ai.LLMResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Parser for handling LLM responses with robust error recovery mechanisms.
 * Handles various response formats including raw JSON, JSON wrapped in markdown
 * code blocks,
 * and responses with additional text before or after the JSON content.
 */
@Slf4j
@Component
public class LLMResponseParser {

    private static final Pattern JSON_PATTERN = Pattern.compile(
            "(?s)(?:```(?:json)?\\s*\\n)?" + // Optional code block with optional json language specifier
                    "([\\s\\S]*?)" + // The actual JSON content (non-greedy)
                    "(?:\\n```)?" + // Optional closing code block
                    "$", // End of string
            Pattern.MULTILINE);

    private final ObjectMapper objectMapper;

    public LLMResponseParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Parse the raw LLM response string into an LLMResponse object.
     * Attempts to extract and parse JSON from the response using multiple
     * strategies.
     * 
     * @param rawResponse The raw string response from the LLM
     * @return LLMResponse containing both raw content and parsed JSON (if any)
     */
    public LLMResponse parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return LLMResponse.builder()
                    .rawContent("")
                    .parsedJson(null)
                    .build();
        }

        // Clean and normalize the response
        String cleanedResponse = cleanResponse(rawResponse);

        // Try to parse the full response as JSON first
        Optional<JsonNode> jsonNode = tryParseJson(cleanedResponse);

        // If that fails, try to extract JSON from markdown code blocks or other
        // patterns
        if (jsonNode.isEmpty()) {
            Matcher matcher = JSON_PATTERN.matcher(cleanedResponse);
            if (matcher.find() && matcher.group(1) != null) {
                String possibleJson = matcher.group(1).trim();
                jsonNode = tryParseJson(possibleJson);
            }
        }

        // If we still don't have valid JSON, try to find and extract a JSON object or
        // array
        if (jsonNode.isEmpty()) {
            jsonNode = findAndExtractJson(cleanedResponse);
        }

        return LLMResponse.builder()
                .rawContent(rawResponse)
                .parsedJson(jsonNode.orElse(null))
                .build();
    }

    /**
     * Clean the response string by removing common issues that might prevent JSON
     * parsing.
     * 
     * @param response The raw response string to clean
     * @return The cleaned response string
     */
    private String cleanResponse(String response) {
        if (response == null) {
            return "";
        }

        String cleaned = response;

        // Remove BOM if present
        if (cleaned.startsWith("\uFEFF")) {
            cleaned = cleaned.substring(1);
        }

        // Remove any trailing commas before closing brackets/braces
        cleaned = cleaned.replaceAll(",\\s*([}\\]])", "$1");

        // Remove any non-printable characters except newlines and tabs
        cleaned = cleaned.replaceAll("[\\p{C}&&[^\\n\\r\\t]]", "");

        return cleaned.trim();
    }

    /**
     * Attempt to parse a string as JSON, returning an empty Optional if parsing
     * fails.
     * 
     * @param jsonString The string to parse as JSON
     * @return Optional containing the parsed JsonNode if successful, empty
     *         otherwise
     */
    private Optional<JsonNode> tryParseJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readTree(jsonString));
        } catch (JsonProcessingException e) {
            log.debug("Failed to parse JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Attempt to find and extract a JSON object or array from a string that may
     * contain
     * additional text before or after the JSON.
     * 
     * @param text The text potentially containing JSON
     * @return Optional containing the parsed JsonNode if found and valid, empty
     *         otherwise
     */
    private Optional<JsonNode> findAndExtractJson(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Optional.empty();
        }

        // Look for JSON object or array
        int startBrace = text.indexOf('{');
        int startBracket = text.indexOf('[');

        // Determine which comes first (if any)
        int startIndex = -1;
        char startChar = '\0';

        if (startBrace >= 0 && (startBrace < startBracket || startBracket < 0)) {
            startIndex = startBrace;
            startChar = '{';
        } else if (startBracket >= 0) {
            startIndex = startBracket;
            startChar = '[';
        }

        if (startIndex < 0) {
            return Optional.empty();
        }

        // Find the matching closing brace/bracket
        char endChar = (startChar == '{') ? '}' : ']';
        int openCount = 1;
        int endIndex = -1;

        for (int i = startIndex + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == startChar) {
                openCount++;
            } else if (c == endChar) {
                openCount--;
                if (openCount == 0) {
                    endIndex = i;
                    break;
                }
            } else if (c == '"' && i > 0 && text.charAt(i - 1) != '\\') {
                // Skip string literals
                i++;
                while (i < text.length()) {
                    if (text.charAt(i) == '"' && text.charAt(i - 1) != '\\') {
                        break;
                    }
                    i++;
                }
            }
        }

        if (endIndex > startIndex) {
            String possibleJson = text.substring(startIndex, endIndex + 1);
            return tryParseJson(possibleJson);
        }

        return Optional.empty();
    }
}
