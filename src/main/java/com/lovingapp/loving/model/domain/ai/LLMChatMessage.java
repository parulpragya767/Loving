package com.lovingapp.loving.model.domain.ai;

import com.lovingapp.loving.model.enums.ChatMessageRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMChatMessage {
    private ChatMessageRole role;
    private String content;
}
