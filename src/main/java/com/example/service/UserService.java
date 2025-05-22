package com.example.service;

import com.example.dto.UserRegistrationDTO;
import com.example.dto.UserProfileDTO;
import com.example.dto.UserEditDTO;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.exception.UserNotFoundException;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.InvalidAdminCodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing user operations in the Zay-Shop application.
 */
@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminCode;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Value("${app.admin.code:NOVA_ADMIN_2025}") String adminCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminCode = adminCode;
    }

    /**
     * Registers a new user with the provided details.
     *
     * @param userDTO   User registration data
     * @param adminCode Admin code for ROLE_ADMIN registration
     * @return The saved user
     * @throws UserAlreadyExistsException if username or email is taken
     * @throws InvalidAdminCodeException if admin code is invalid
     */
    @Transactional
    public User registerUser(@Valid UserRegistrationDTO userDTO, String adminCode) {
        logger.info("Registering user: username={}", userDTO.getUsername());
        logger.debug("User DTO: username={}, email={}, role={}",
                userDTO.getUsername(), userDTO.getEmail(), userDTO.getRole());

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            logger.warn("Username already exists: {}", userDTO.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + userDTO.getUsername());
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.warn("Email already exists: {}", userDTO.getEmail());
            throw new UserAlreadyExistsException("Email already exists: " + userDTO.getEmail());
        }
        if ("ROLE_ADMIN".equals(userDTO.getRole()) &&
                (!StringUtils.hasText(adminCode) || !adminCode.equals(this.adminCode))) {
            logger.warn("Invalid admin code for role: {}", userDTO.getRole());
            throw new InvalidAdminCodeException("Invalid admin registration code");
        }

        User user = new User(
                userDTO.getUsername(),
                passwordEncoder.encode(userDTO.getPassword()),
                userDTO.getEmail(),
                userDTO.getFullName(),
                userDTO.getRole()
        );

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: id={}, username={}",
                savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    /**
     * Updates a user's profile details.
     *
     * @param username   Username of the user
     * @param profileDTO Profile data
     * @return The updated user
     * @throws UserNotFoundException if user is not found
     * @throws UserAlreadyExistsException if email is taken
     */
    @Transactional
    public User updateProfile(String username, @Valid UserProfileDTO profileDTO) {
        logger.info("Updating profile: username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", username);
                    return new UserNotFoundException("User not found: " + username);
                });

        if (!user.getEmail().equals(profileDTO.getEmail()) &&
                userRepository.existsByEmail(profileDTO.getEmail())) {
            logger.warn("Email already exists: {}", profileDTO.getEmail());
            throw new UserAlreadyExistsException("Email already exists: " + profileDTO.getEmail());
        }

        user.setFullName(profileDTO.getFullName());
        user.setEmail(profileDTO.getEmail());

        User updatedUser = userRepository.save(user);
        logger.info("Profile updated: username={}", username);
        return updatedUser;
    }

    /**
     * Updates a user's details by ID.
     *
     * @param id         User ID
     * @param userEditDTO Updated user data
     * @return The updated user
     * @throws UserNotFoundException if user is not found
     * @throws UserAlreadyExistsException if username or email is taken
     */
    @Transactional
    public User updateUser(String id, @Valid UserEditDTO userEditDTO) {
        logger.info("Updating user: id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found: id={}", id);
                    return new UserNotFoundException("User not found with ID: " + id);
                });

        if (!user.getUsername().equals(userEditDTO.getUsername()) &&
                userRepository.existsByUsername(userEditDTO.getUsername())) {
            logger.warn("Username already exists: {}", userEditDTO.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + userEditDTO.getUsername());
        }
        if (!user.getEmail().equals(userEditDTO.getEmail()) &&
                userRepository.existsByEmail(userEditDTO.getEmail())) {
            logger.warn("Email already exists: {}", userEditDTO.getEmail());
            throw new UserAlreadyExistsException("Email already exists: " + userEditDTO.getEmail());
        }

        user.setUsername(userEditDTO.getUsername());
        user.setEmail(userEditDTO.getEmail());
        user.setFullName(userEditDTO.getFullName());
        user.setRole(userEditDTO.getRole() != null ? userEditDTO.getRole() : "ROLE_USER");

        if (StringUtils.hasText(userEditDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userEditDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        logger.info("User updated: id={}", id);
        return updatedUser;
    }

    /**
     * Deletes a user by ID.
     *
     * @param id User ID
     * @throws UserNotFoundException if user is not found
     */
    @Transactional
    public void delete(String id) {
        logger.info("Deleting user: id={}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("User not found: id={}", id);
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        logger.info("User deleted: id={}", id);
    }

    // Unchanged query methods (as per request)
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