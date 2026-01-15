package com.lovingapp.loving.mapper;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.loving.model.entity.ChatSession;

@Component
public final class ChatSessionMapper {

    public static ChatSessionDTO toDto(ChatSession session) {
        if (session == null) {
            return null;
        }

        return ChatSessionDTO.builder()
                .id(session.getId())
                .title(session.getTitle())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
