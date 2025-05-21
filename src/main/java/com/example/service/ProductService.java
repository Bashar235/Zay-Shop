package com.example.service;

import com.example.model.Comment;
import com.example.model.Product;
import com.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private static final int PAGE_SIZE = 9;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> findAll(Pageable pageable, String sort) {
        if (pageable == null) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Pageable sortedPageable = pageable;
        if (sort != null) {
            switch (sort) {
                case "priceAsc":
                    sortedPageable = PageRequest.of(pageable.getPageNumber(), PAGE_SIZE, Sort.by("price").ascending());
                    break;
                case "priceDesc":
                    sortedPageable = PageRequest.of(pageable.getPageNumber(), PAGE_SIZE, Sort.by("price").descending());
                    break;
                case "rating":
                    sortedPageable = PageRequest.of(pageable.getPageNumber(), PAGE_SIZE, Sort.by("rating").descending());
                    break;
            }
        }
        return productRepository.findAll(sortedPageable);
    }

    public Product findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return product;
    }

    public Page<Product> findByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findTop3ByOrderByRatingDesc();
    }

    public List<Product> findRelatedProducts(String category, String productId) {
        return productRepository.findTop4ByCategoryAndIdNot(category, productId);
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.findBySearchText(query, pageable);
    }

    public void bookmarkProduct(String productId, String username) {
        Product product = findById(productId);
        List<String> bookmarks = product.getBookmarks();
        if (!bookmarks.contains(username)) {
            bookmarks.add(username);
            product.setBookmarks(bookmarks);
            productRepository.save(product);
        }
    }

    public void removeBookmark(String productId, String username) {
        Product product = findById(productId);
        List<String> bookmarks = product.getBookmarks();
        bookmarks.remove(username);
        product.setBookmarks(bookmarks);
        productRepository.save(product);
    }

    public List<Product> findBookmarkedProducts(String username) {
        return productRepository.findByBookmarksContaining(username);
    }

    public void addComment(String productId, String username, String content) {
        Product product = findById(productId);
        List<Comment> comments = product.getComments();
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setUsername(username);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        comments.add(comment);
        product.setComments(comments);
        product.setReviewCount(comments.size());
        productRepository.save(product);
    }

    public void save(Product product) {
        if (product.getRating() == null) {
            product.setRating(0.0);
        }
        productRepository.save(product);
    }

    public void deleteById(String id) {
        productRepository.deleteById(id);
    }
}