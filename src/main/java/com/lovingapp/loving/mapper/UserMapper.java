package com.lovingapp.loving.mapper;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.model.dto.UserDTO;
import com.lovingapp.loving.model.entity.User;

@Component
public final class UserMapper {

    public static UserDTO toDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserDTO.builder()
                .id(entity.getId())
                .authUserId(entity.getAuthUserId())
                .email(entity.getEmail())
                .displayName(entity.getDisplayName())
                .onboardingCompleted(entity.getOnboardingCompleted())
                .lastLoginAt(entity.getLastLoginAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .id(dto.getId())
                .authUserId(dto.getAuthUserId())
                .email(dto.getEmail())
                .displayName(dto.getDisplayName())
                .onboardingCompleted(dto.getOnboardingCompleted())
                .lastLoginAt(dto.getLastLoginAt())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static void updateEntityFromDto(UserDTO dto, User entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }

        if (dto.getDisplayName() != null) {
            entity.setDisplayName(dto.getDisplayName());
        }
    }
}
