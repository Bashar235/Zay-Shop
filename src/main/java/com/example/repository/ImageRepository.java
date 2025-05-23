package com.example.repository;

import com.example.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ImageRepository extends MongoRepository<Image, String> {
    Optional<Image> findByName(String name);
}