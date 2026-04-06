package com.lovingapp.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.exception.ResourceAlreadyExistsException;
import com.lovingapp.exception.ResourceNotFoundException;
import com.lovingapp.mapper.UserMapper;
import com.lovingapp.model.dto.UserDTOs.UserDTO;
import com.lovingapp.model.dto.UserDTOs.UserUpdateRequest;
import com.lovingapp.model.entity.User;
import com.lovingapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;

    /*
     * Returns the user object associated with the authUserId (which is the external
     * supabase user id).
     * If there is no user found, it will create a new user object and return it.
     */
    @Transactional
    public UserDTO syncUser(UUID authUserId, String email) {
        Optional<User> user = userRepository.findByAuthUserId(authUserId);

        if (user.isEmpty()) {
            if (userRepository.existsByEmail(email)) {
                throw new ResourceAlreadyExistsException(
                        "Email exists but is associated with a different auth identity");
            }

            log.info("Creating new user profile for first login authUserId={}", authUserId);
            User newUser = User.builder()
                    .authUserId(authUserId)
                    .email(email)
                    .lastLoginAt(OffsetDateTime.now())
                    .build();
            User saved = userRepository.save(newUser);
            return UserMapper.toDto(saved);
        }

        User existing = user.get();
        existing.setLastLoginAt(OffsetDateTime.now());
        if (email != null && !email.equals(existing.getEmail())) {
            existing.setEmail(email);
        }
        User saved = userRepository.save(existing);
        return UserMapper.toDto(saved);
    }

    /*
     * Returns the user object associated with the authUserId (which is the external
     * supabase user id).
     * If there is no user found, it will throw a ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByAuthUserId(UUID authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "authUserId", authUserId));
    }

    @Transactional
    public void updateUser(UUID userId, UserUpdateRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserMapper.updateEntity(request, existingUser);
        userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("Deleting user and all associated data userId={}, email={}", userId, user.getEmail());

        try {
            supabaseAuthService.deleteAuthUser(user.getAuthUserId());
            log.info("Successfully deleted Supabase auth account for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to delete Supabase auth account for userId={}, authUserId={}",
                    userId, user.getAuthUserId());
            throw new RuntimeException("Failed to delete Supabase auth account. Please retry.", e);
        }

        userRepository.delete(user);

        log.info("User and all associated data deleted successfully userId={}", userId);
    }
}
