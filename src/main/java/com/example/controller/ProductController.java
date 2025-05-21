package com.example.controller;

import com.example.model.Image;
import com.example.model.Product;
import com.example.model.Category;
import com.example.service.CategoryService;
import com.example.service.ImageService;
import com.example.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // Import Optional

@Controller
@RequestMapping("/shop")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ImageService imageService;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, ImageService imageService, CategoryService categoryService) {
        this.productService = productService;
        this.imageService = imageService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String shop(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) String categoryId, // Changed parameter name to categoryId
                       @RequestParam(required = false, defaultValue = "featured") String sort,
                       @RequestParam(required = false) String q,
                       Model model) {
        Pageable pageable = PageRequest.of(page - 1, 9);
        Page<Product> products;

        if (q != null && !q.isEmpty()) {
            products = productService.searchProducts(q, pageable);
        } else if (categoryId != null && !categoryId.isEmpty()) { // Use categoryId
            // Fetch category by ID
            Optional<Category> categoryEntityOptional = Optional.ofNullable(categoryService.findById(categoryId));
            if (categoryEntityOptional.isPresent()) {
                products = productService.findByCategory(categoryId, pageable); // Pass categoryId directly

            } else {
                products = Page.empty();  // No products found if the category doesn't exist
            }
        } else {
            products = productService.findAll(pageable, sort);
        }

        if (products.isEmpty() && (categoryId != null || q != null)) { // Use categoryId
            logger.warn("No products found for query: q={}, categoryId={}", q, categoryId); // Use categoryId
        }

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("categoryId", categoryId); // Pass categoryId to the model
        model.addAttribute("q", q);
        model.addAttribute("categories", categoryService.findAll());

        addImageAttributes(model);
        return "shop";
    }

    @GetMapping("/category/{categoryId}")
    public String categoryShop(@PathVariable String categoryId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false, defaultValue = "featured") String sort,
                               Model model) {
        Pageable pageable = PageRequest.of(page - 1, 9);

        // Fetch category by ID
        Optional<Category> categoryEntityOptional = Optional.ofNullable(categoryService.findById(categoryId));
        if (categoryEntityOptional.isPresent()) {
            Page<Product> products = productService.findByCategory(categoryId, pageable); // Pass categoryId directly
            if (products.isEmpty()) {
                logger.warn("No products found for category: {}", categoryId);
            }

            model.addAttribute("products", products.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("sort", sort);
            model.addAttribute("categoryId", categoryId);  // Pass the categoryId
        } else {
            // If the category doesn't exist, handle it gracefully
            logger.warn("Category not found for ID: {}", categoryId);
            model.addAttribute("products", List.of()); // Empty product list
            model.addAttribute("categoryId", categoryId); // Show the categoryId
        }

        model.addAttribute("categories", categoryService.findAll());
        addImageAttributes(model);
        return "shop";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable String id, Model model) {
        Product product = productService.findById(id);
        List<Product> relatedProducts = productService.findRelatedProducts(product.getCategory(), id);

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);

        // Updated: Use addImageAttributes for consistency
        addImageAttributes(model);
        return "shop-single";
    }

    @PostMapping("/product/{id}/comment")
    public String addComment(@PathVariable String id,
                             @RequestParam String content,
                             Authentication authentication) {
        if (authentication != null) {
            productService.addComment(id, authentication.getName(), content);
            logger.info("User {} commented on product {}", authentication.getName(), id);
        }
        return "redirect:/shop/product/" + id;
    }

    @PostMapping("/product/{id}/bookmark")
    public String bookmark(@PathVariable String id, Authentication authentication) {
        if (authentication != null) {
            productService.bookmarkProduct(id, authentication.getName());
            logger.info("User {} bookmarked product {}", authentication.getName(), id);
        }
        return "redirect:/shop/product/" + id;
    }

    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        // Updated: Only add brand images if used in shop.html or shop-single.html
        Image brand1 = imageService.findByName("brand_01.png");
        Image brand2 = imageService.findByName("brand_02.png");
        Image brand3 = imageService.findByName("brand_03.png");
        Image brand4 = imageService.findByName("brand_04.png");

        model.addAttribute("logoImage", logoImage != null ? logoImage : createEmptyImage("apple-icon.png"));
        model.addAttribute("faviconImage", faviconImage != null ? faviconImage : createEmptyImage("favicon.ico"));
        model.addAttribute("brand1", brand1 != null ? brand1 : createEmptyImage("brand_01.png"));
        model.addAttribute("brand2", brand2 != null ? brand2 : createEmptyImage("brand_02.png"));
        model.addAttribute("brand3", brand3 != null ? brand3 : createEmptyImage("brand_03.png"));
        model.addAttribute("brand4", brand4 != null ? brand4 : createEmptyImage("brand_04.png"));

        logger.debug("Added image attributes for shop page");
    }

    // New: Helper method for empty Image fallback
    private Image createEmptyImage(String name) {
        logger.warn("Image not found for name: {}", name);
        Image image = new Image();
        image.setName(name);
        image.setContentType("image/png");
        image.setData(new byte[0]);
        return image;
    }
}