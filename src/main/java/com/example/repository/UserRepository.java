package com.example.repository;

import com.example.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(@NotBlank(message = "Username is required") @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters") String username);

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);
}