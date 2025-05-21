package com.example.service;

import com.example.model.ContactSubmission;
import com.example.repository.ContactSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactSubmissionRepository repository;

    public ContactService(ContactSubmissionRepository repository) {
        this.repository = repository;
    }

    public void saveSubmission(ContactSubmission submission) {
        repository.save(submission);
    }

    public List<ContactSubmission> findAll() {
        return repository.findAll();
    }
}