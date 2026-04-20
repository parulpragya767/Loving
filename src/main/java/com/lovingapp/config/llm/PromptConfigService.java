package com.lovingapp.config.llm;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lovingapp.config.llm.LlmClientProperties.OpenAiProperties.Prompt;
import com.lovingapp.constants.PromptConfigConstants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromptConfigService {

    private final LlmClientProperties properties;

    public PromptConfig getEmpatheticChatResponse() {
        Prompt prompt = properties.getOpenai().getPrompts().getEmpatheticChatResponse();
        return new PromptConfig(
                prompt.getId(),
                prompt.getVersion(),
                prompt.getName(),
                List.of(PromptConfigConstants.CONVERSATION_VARIABLE,
                        PromptConfigConstants.LATEST_USER_MESSAGE_VARIABLE,
                        PromptConfigConstants.CURRENT_TURN_VARIABLE));
    }

    public PromptConfig getUserContextExtraction() {
        Prompt prompt = properties.getOpenai().getPrompts().getUserContextExtraction();
        return new PromptConfig(
                prompt.getId(),
                prompt.getVersion(),
                prompt.getName(),
                List.of(PromptConfigConstants.CONVERSATION_VARIABLE));
    }

    public PromptConfig getWrapUpChatResponse() {
        Prompt prompt = properties.getOpenai().getPrompts().getWrapUpChatResponse();
        return new PromptConfig(
                prompt.getId(),
                prompt.getVersion(),
                prompt.getName(),
                List.of(PromptConfigConstants.CONVERSATION_VARIABLE,
                        PromptConfigConstants.RECOMMENDED_RITUAL_PACK_VARIABLE));
    }
}
