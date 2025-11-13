package com.lovingapp.loving.client;

import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;

public interface LlmClient {

    <T> LLMResponse<T> generate(LLMRequest request, Class<T> responseClass);

    default LLMResponse<String> generate(LLMRequest request) {
        return generate(request, String.class);
    }
}
