package com.lovingapp.loving.mapper;

import java.util.UUID;

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

    public static ChatSessionDTO toSummaryDto(ChatSession session) {
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

    public static ChatSession fromDto(ChatSessionDTO dto, UUID userId) {
        if (dto == null) {
            return null;
        }

        return ChatSession.builder()
                .id(dto.getId())
                .userId(userId)
                .title(dto.getTitle())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
