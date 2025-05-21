package com.example.repository;

import com.example.model.ContactSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactSubmissionRepository extends MongoRepository<ContactSubmission, String> {
}