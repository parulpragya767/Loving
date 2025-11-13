package com.lovingapp.loving.model.domain.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMResponse<T> {
    private String rawText; // The raw model text output
    private T parsed; // Parsed json object
}