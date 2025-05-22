package com.example.service;

import com.example.dto.UserRegistrationDTO;
import com.example.dto.UserProfileDTO;
import com.example.dto.UserEditDTO;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminCode;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, @Value("${app.admin.code}") String adminCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminCode = adminCode;
    }

    public User registerUser(@Valid UserRegistrationDTO userDTO, String adminCode) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists: " + userDTO.getUsername());
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + userDTO.getEmail());
        }
        if ("ROLE_ADMIN".equals(userDTO.getRole()) && !this.adminCode.equals(adminCode)) {
            throw new IllegalStateException("Invalid admin registration code");
        }
        User user = new User(
                userDTO.getUsername(),
                passwordEncoder.encode(userDTO.getPassword()),
                userDTO.getEmail(),
                userDTO.getFullName(),
                userDTO.getRole()
        );
        try {
            User savedUser = userRepository.save(user);
            logger.info("User saved successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to save user: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to save user: " + e.getMessage());
        }
    }

    public User updateProfile(String username, @Valid UserProfileDTO profileDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (!user.getEmail().equals(profileDTO.getEmail()) &&
                userRepository.findByEmail(profileDTO.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + profileDTO.getEmail());
        }
        user.setFullName(profileDTO.getFullName());
        user.setEmail(profileDTO.getEmail());
        return userRepository.save(user);
    }

    public User updateUser(String id, UserEditDTO updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + id));

        // Check for unique username (if changed)
        if (!user.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists: " + updatedUser.getUsername());
        }

        // Check for unique email (if changed)
        if (!user.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + updatedUser.getEmail());
        }

        // Update fields
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setFullName(updatedUser.getFullName());
        user.setRole(updatedUser.getRole() != null ? updatedUser.getRole() : "ROLE_USER");

        // Update password only if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(user);
    }

    public void delete(String id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }


}