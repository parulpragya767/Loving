package com.lovingapp.loving.client;

import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;

public interface LlmClient {

    LLMResponse generate(LLMRequest request);
}
