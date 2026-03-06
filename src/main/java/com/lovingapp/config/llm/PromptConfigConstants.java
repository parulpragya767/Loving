package com.lovingapp.config.llm;

import java.util.List;

public final class PromptConfigConstants {

    public static final String CONVERSATION_VARIABLE = "conversation";
    public static final String RECOMMENDED_RITUAL_PACK_VARIABLE = "recommended_ritual_pack";
    public static final String LATEST_USER_MESSAGE_VARIABLE = "latest_user_message";

    public static final PromptConfig EMPATHETIC_CHAT_RESPONSE = new PromptConfig(
            "pmpt_699fd785fc34819087b33231cd9060ff0e4ac7e54210cd79",
            "3",
            List.of(CONVERSATION_VARIABLE, LATEST_USER_MESSAGE_VARIABLE));

    public static final PromptConfig USER_CONTEXT_EXTRACTION = new PromptConfig(
            "user_context_extraction_prompt_id",
            "1.0",
            List.of(CONVERSATION_VARIABLE));

    public static final PromptConfig WRAP_UP_CHAT_RESPONSE = new PromptConfig(
            "wrap_up_chat_response_prompt_id",
            "1.0",
            List.of(CONVERSATION_VARIABLE, RECOMMENDED_RITUAL_PACK_VARIABLE));

    private PromptConfigConstants() {
    }
}
