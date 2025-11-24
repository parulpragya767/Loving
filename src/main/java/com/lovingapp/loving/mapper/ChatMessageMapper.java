package com.lovingapp.loving.mapper;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.model.dto.ChatDTOs.ChatMessageDTO;
import com.lovingapp.loving.model.entity.ChatMessage;

@Component
public final class ChatMessageMapper {

    public static ChatMessageDTO toDto(ChatMessage message) {
        if (message == null) {
            return null;
        }

        return ChatMessageDTO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .role(message.getRole())
                .content(message.getContent())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public static ChatMessage fromDto(ChatMessageDTO dto) {
        if (dto == null) {
            return null;
        }

        return ChatMessage.builder()
                .id(dto.getId())
                .sessionId(dto.getSessionId())
                .role(dto.getRole())
                .content(dto.getContent())
                .metadata(dto.getMetadata())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
