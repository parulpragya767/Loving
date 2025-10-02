package com.lovingapp.loving.mapper;

import com.lovingapp.loving.dto.UserDTO;
import com.lovingapp.loving.model.User;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for converting between User entity and DTO.
 */
@Component
public class UserMapper {

    /**
     * Converts a User entity to a UserDTO.
     * @param entity the entity to convert
     * @return the converted DTO, or null if the input is null
     */
    public UserDTO toDto(User entity) {
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

    /**
     * Converts a UserDTO to a User entity.
     * @param dto the DTO to convert
     * @return the converted entity, or null if the input is null
     */
    public User toEntity(UserDTO dto) {
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
}
