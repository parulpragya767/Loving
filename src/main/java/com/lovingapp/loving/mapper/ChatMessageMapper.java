package com.lovingapp.loving.mapper;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.model.dto.ChatDTOs;
import com.lovingapp.loving.model.entity.ChatMessage;

@Component
public final class ChatMessageMapper {

    public static ChatDTOs.ChatMessageDTO toDto(ChatMessage message) {
        if (message == null) {
            return null;
        }

        return ChatDTOs.ChatMessageDTO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public static ChatMessage fromDto(ChatDTOs.ChatMessageDTO dto) {
        if (dto == null) {
            return null;
        }

        return ChatMessage.builder()
                .id(dto.getId())
                .sessionId(dto.getSessionId())
                .role(dto.getRole())
                .content(dto.getContent())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
