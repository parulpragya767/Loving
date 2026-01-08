package com.lovingapp.loving.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.UserMapper;
import com.lovingapp.loving.model.dto.UserDTO;
import com.lovingapp.loving.model.entity.User;
import com.lovingapp.loving.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDTO syncUser(UUID authUserId, String email, String displayName) {
        return userRepository.findByAuthUserId(authUserId)
                .map(existing -> {
                    existing.setLastLoginAt(OffsetDateTime.now());
                    if (email != null && !email.equals(existing.getEmail())) {
                        existing.setEmail(email);
                    }
                    if (displayName != null && !displayName.equals(existing.getDisplayName())) {
                        existing.setDisplayName(displayName);
                    }
                    User saved = userRepository.save(existing);
                    return UserMapper.toDto(saved);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .authUserId(authUserId)
                            .email(email)
                            .displayName(displayName)
                            .lastLoginAt(OffsetDateTime.now())
                            .build();
                    User saved = userRepository.save(newUser);
                    return UserMapper.toDto(saved);
                });
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByAuthUserId(UUID authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with authUserId: " + authUserId));
    }

    @Transactional
    public UserDTO updateUser(UUID userId, UserDTO userDTO) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    UserMapper.updateEntityFromDto(userDTO, existingUser);
                    User updatedUser = userRepository.save(existingUser);
                    return UserMapper.toDto(updatedUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
