package com.lovingapp.loving.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.dto.UserDTO;
import com.lovingapp.loving.model.User;

@Component
public final class UserMapper {

    public static UserDTO toDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserDTO.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .displayName(entity.getDisplayName())
                .phoneNumber(entity.getPhoneNumber())
                .dateOfBirth(entity.getDateOfBirth() != null ? entity.getDateOfBirth().toString() : null)
                .gender(entity.getGender())
                .isEmailVerified(entity.getIsEmailVerified())
                .isActive(entity.getIsActive())
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
                .id(dto.getId() != null ? UUID.fromString(dto.getId()) : null)
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .displayName(dto.getDisplayName())
                .phoneNumber(dto.getPhoneNumber())
                .gender(dto.getGender())
                .isEmailVerified(dto.getIsEmailVerified() != null ? dto.getIsEmailVerified() : false)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }

    public static void updateEntityFromDto(UserDTO dto, User entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getFirstName() != null) {
            entity.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            entity.setLastName(dto.getLastName());
        }
        if (dto.getDisplayName() != null) {
            entity.setDisplayName(dto.getDisplayName());
        }
        if (dto.getPhoneNumber() != null) {
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getGender() != null) {
            entity.setGender(dto.getGender());
        }
        if (dto.getIsEmailVerified() != null) {
            entity.setIsEmailVerified(dto.getIsEmailVerified());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}
