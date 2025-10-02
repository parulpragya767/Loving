package com.lovingapp.loving.service;

import com.lovingapp.loving.model.User;
import com.lovingapp.loving.repository.UserRepository;
import com.lovingapp.loving.dto.UserDTO;
import com.lovingapp.loving.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDTO updateUser(String id, UserDTO userDTO) {
        User existingUser = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        // Update fields from DTO
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setDisplayName(userDTO.getDisplayName());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setGender(userDTO.getGender());
        existingUser.setIsEmailVerified(userDTO.getIsEmailVerified());
        existingUser.setIsActive(userDTO.getIsActive());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(UUID.fromString(id))) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(UUID.fromString(id));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
