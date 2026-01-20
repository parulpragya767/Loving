package com.lovingapp.loving.client;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.config.llm.LlmClientProperties.OpenAiProperties;
import com.lovingapp.loving.exception.LLMException;
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

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiChatClient(OpenAiProperties openAiProperties) {
        this.openAiProperties = openAiProperties;
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

                log.info("LLM request: model={}, format={}, systemPrompt.len={}, messages.size={}",
                        getLLMModel(request),
                        request.getResponseFormat(),
                        request.getSystemPrompt() == null ? 0 : request.getSystemPrompt().length(),
                        request.getMessages() == null ? 0 : request.getMessages().size());

                var response = client.responses().create(params);

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

                log.info("LLM response(JSON): parsedClass={}, text.len={}",
                        (parsed == null ? null : parsed.getClass().getSimpleName()),
                        rawText == null ? 0 : rawText.length());
            } else {
                ResponseCreateParams params = buildResponseCreateParams(request)
                        .build();

                log.info("LLM request: model={}, format={}, systemPrompt.len={}, messages.size={}",
                        getLLMModel(request),
                        request.getResponseFormat(),
                        request.getSystemPrompt() == null ? 0 : request.getSystemPrompt().length(),
                        request.getMessages() == null ? 0 : request.getMessages().size());

                var response = client.responses().create(params);

                rawText = response.output().stream()
                        .flatMap(item -> item.message().stream())
                        .flatMap(message -> message.content().stream())
                        .flatMap(content -> content.outputText().stream())
                        .findFirst()
                        .map(outputText -> outputText.text())
                        .orElse("");

                log.info("LLM response(TEXT): text.len={}",
                        rawText == null ? 0 : rawText.length());
            }
            return new LLMResponse<>(rawText, parsed);

        } catch (Exception e) {
            log.error("OpenAI LLM request failed: model={} format={} responseClass={}",
                    getLLMModel(request),
                    request == null ? null : request.getResponseFormat(),
                    responseClass == null ? null : responseClass.getSimpleName());
            throw new LLMException("LLM request failed", e);
        }
    }

    private ResponseCreateParams.Builder buildResponseCreateParams(LLMRequest request) {
        return ResponseCreateParams.builder()
                .model(getLLMModel(request))
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

        if (openAiProperties.getApiKey() != null && !openAiProperties.getApiKey().isBlank()) {
            builder.apiKey(openAiProperties.getApiKey());
        }

        return builder.build();
    }

    private String getLLMModel(LLMRequest request) {
        return request.getModel() != null && !request.getModel().isEmpty() ? request.getModel()
                : openAiProperties.getModel();
    }
}
