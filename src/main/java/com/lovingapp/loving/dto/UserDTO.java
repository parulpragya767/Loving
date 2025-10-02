package com.lovingapp.loving.dto;

import lombok.*;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for User entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private Boolean isEmailVerified;
    private Boolean isActive;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
