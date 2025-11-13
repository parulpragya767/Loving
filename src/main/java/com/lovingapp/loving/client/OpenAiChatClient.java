package com.lovingapp.loving.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.config.LlmClientProperties;
import com.lovingapp.loving.model.domain.ai.LLMChatMessage;
import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;
import com.lovingapp.loving.model.domain.ai.LLMResponseFormat;
import com.lovingapp.loving.model.enums.ChatMessageRole;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.StructuredResponseCreateParams;

import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI implementation of the LlmClient interface.
 */
@Slf4j
public class OpenAiChatClient implements LlmClient {

    private final LlmClientProperties.OpenAiProperties openAiProps;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiChatClient(LlmClientProperties llmClientProperties) {
        this.openAiProps = llmClientProperties.getOpenai();
    }

    @Override
    public <T> LLMResponse<T> generate(LLMRequest request, Class<T> responseClass) {
        try {

            OpenAIClient client = buildClientFromProps();

            String rawText = "";
            T parsed = null;

            if (request.getResponseFormat() == LLMResponseFormat.JSON && responseClass != null
                    && !String.class.equals(responseClass)) {
                StructuredResponseCreateParams<T> params = buildResponseCreateParams(request)
                        .text(responseClass)
                        .build();

                try {
                    log.info("Sending request to OpenAI API: {}",
                            objectMapper.writeValueAsString(buildRequestLog(request)));
                } catch (Exception e) {
                    log.error("Error logging request: " + e.getMessage());
                }

                var response = client.responses().create(params);

                try {
                    List<String> outputs = response.output().stream()
                            .flatMap(item -> item.message().stream())
                            .flatMap(message -> message.content().stream())
                            .flatMap(content -> content.outputText().stream())
                            .map(this::truncateObject)
                            .collect(Collectors.toList());
                    Map<String, Object> respLog = new HashMap<>();
                    respLog.put("outputs", outputs);
                    log.info("Response from OpenAI API: {}", objectMapper.writeValueAsString(respLog));
                } catch (Exception e) {
                    log.error("Error logging response: " + e.getMessage());
                }

                parsed = response.output().stream()
                        .flatMap(item -> item.message().stream())
                        .flatMap(message -> message.content().stream())
                        .flatMap(content -> content.outputText().stream())
                        .findFirst()
                        .orElse(null);

                if (parsed != null) {
                    try {
                        rawText = objectMapper.writeValueAsString(parsed);
                    } catch (Exception e) {
                        rawText = parsed.toString();
                    }
                } else {
                    rawText = "";
                }
            } else {
                ResponseCreateParams params = buildResponseCreateParams(request)
                        .build();

                try {
                    log.info("Sending request to OpenAI API: {}",
                            objectMapper.writeValueAsString(buildRequestLog(request)));
                } catch (Exception e) {
                    log.error("Error logging request: " + e.getMessage());
                }

                var response = client.responses().create(params);

                try {
                    List<String> outputs = response.output().stream()
                            .flatMap(item -> item.message().stream())
                            .flatMap(message -> message.content().stream())
                            .flatMap(content -> content.outputText().stream())
                            .map(this::truncateObject)
                            .collect(Collectors.toList());
                    Map<String, Object> respLog = new HashMap<>();
                    respLog.put("outputs", outputs);
                    log.info("Response from OpenAI API: {}", objectMapper.writeValueAsString(respLog));
                } catch (Exception e) {
                    log.error("Error logging response: " + e.getMessage());
                }

                rawText = response.output().stream()
                        .flatMap(item -> item.message().stream())
                        .flatMap(message -> message.content().stream())
                        .flatMap(content -> content.outputText().stream())
                        .findFirst()
                        .map(outputText -> outputText.text())
                        .orElse("");
            }
            return new LLMResponse<>(rawText, parsed);

        } catch (Exception e) {
            throw new RuntimeException("Error generating LLM response", e);
        }
    }

    private ResponseCreateParams.Builder buildResponseCreateParams(LLMRequest request) {
        return ResponseCreateParams.builder()
                .model(request.getModel() != null && !request.getModel().isEmpty() ? request.getModel()
                        : openAiProps.getModel())
                .inputOfResponse(buildInputItems(request));
    }

    private List<ResponseInputItem> buildInputItems(LLMRequest request) {
        List<ResponseInputItem> inputItems = new ArrayList<>();

        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.DEVELOPER)
                    .content(request.getSystemPrompt())
                    .build()));
        }

        if (request.getMessages() != null) {
            for (LLMChatMessage msg : request.getMessages()) {
                EasyInputMessage.Role role = switch (msg.getRole()) {
                    case ChatMessageRole.USER -> EasyInputMessage.Role.USER;
                    case ChatMessageRole.ASSISTANT -> EasyInputMessage.Role.ASSISTANT;
                    case ChatMessageRole.SYSTEM -> EasyInputMessage.Role.DEVELOPER;
                };
                inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
                        .role(role)
                        .content(msg.getContent())
                        .build()));
            }
        }

        return inputItems;
    }

    private OpenAIClient buildClientFromProps() {
        OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder().fromEnv();

        if (openAiProps.getApiKey() != null && !openAiProps.getApiKey().isBlank()) {
            builder.apiKey(openAiProps.getApiKey());
        }

        return builder.build();
    }

    private Map<String, Object> buildRequestLog(LLMRequest request) {
        int messageCount = request.getMessages() == null ? 0 : request.getMessages().size();
        List<Map<String, Object>> msgs = request.getMessages() == null ? List.of()
                : request.getMessages().stream()
                        .map(m -> {
                            Map<String, Object> mm = new HashMap<>();
                            if (m.getRole() != null)
                                mm.put("role", m.getRole().name());
                            if (m.getContent() != null)
                                mm.put("content", truncate(m.getContent()));
                            return mm;
                        })
                        .collect(Collectors.toList());
        Map<String, Object> root = new HashMap<>();
        if (request.getModel() != null)
            root.put("model", request.getModel());
        if (request.getResponseFormat() != null)
            root.put("responseFormat", request.getResponseFormat().name());
        if (request.getSystemPrompt() != null)
            root.put("systemPrompt", truncate(request.getSystemPrompt()));
        root.put("messageCount", messageCount);
        root.put("messages", msgs);
        return root;
    }

    private String truncate(String s) {
        if (s == null)
            return null;
        int max = 8000;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String truncateObject(Object o) {
        if (o == null)
            return null;
        try {
            String s = (o instanceof String) ? (String) o : objectMapper.writeValueAsString(o);
            return truncate(s);
        } catch (Exception ex) {
            return truncate(String.valueOf(o));
        }
    }
}
