package com.example.repository;

import com.example.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategory(String category);

    Page<Product> findByCategory(String category, Pageable pageable);

    List<Product> findTop3ByOrderByRatingDesc();

    List<Product> findTop4ByCategoryAndIdNot(String category, String id);

    long countByCategory(String category);

    @Query("{'$text': {'$search': ?0}}")
    Page<Product> findBySearchText(String searchText, Pageable pageable);

    List<Product> findByBookmarksContaining(String username);
}