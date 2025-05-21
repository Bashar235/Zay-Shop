package com.example.controller;

import com.example.dto.OrderDTO;
import com.example.dto.UserEditDTO;
import com.example.model.Category;
import com.example.model.Image;
import com.example.model.Product;
import com.example.model.User;
import com.example.service.CategoryService;
import com.example.service.ImageService;
import com.example.service.OrderService;
import com.example.service.ProductService;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public AdminController(ProductService productService, CategoryService categoryService,
                           ImageService imageService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Product> products = productService.findAll(null, null).getContent();
        List<Map<String, Object>> productData = products.stream().map(product -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", product.getId());
            data.put("name", product.getName());
            data.put("price", product.getPrice());
            data.put("stockQuantity", product.getStockQuantity());
            String categoryName = "N/A";
            if (product.getCategory() != null) {
                Category category = categoryService.findById(product.getCategory());
                categoryName = category != null ? category.getName() : "Unknown";
            }
            data.put("categoryName", categoryName);
            logger.info("Product ID: {}, Name: {}, Category: {}",
                    product.getId(), product.getName(), categoryName);
            if (product.getId() == null || product.getId().contains("{")) {
                logger.warn("Invalid product ID detected: {}", product.getId());
            }
            return data;
        }).collect(Collectors.toList());
        model.addAttribute("products", productData);
        addImageAttributes(model);
        return "admin/admin-dashboard";
    }

    @GetMapping("/admin-dashboard")
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        addImageAttributes(model);
        return "admin/add-product";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!imageFile.isEmpty()) {
                Image savedImage = imageService.storeImage(imageFile);
                product.setImageId(savedImage.getId());
            } else {
                redirectAttributes.addFlashAttribute("error", "Please upload an image for the product.");
                return "redirect:/admin/products/add";
            }

            if (product.getSizes() != null && !product.getSizes().isEmpty() && product.getSizes().get(0) != null) {
                product.setSizes(Arrays.stream(product.getSizes().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                product.setSizes(Collections.emptyList());
            }

            if (product.getColors() != null && !product.getColors().isEmpty() && product.getColors().get(0) != null) {
                product.setColors(Arrays.stream(product.getColors().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                product.setColors(Collections.emptyList());
            }

            if (product.getSpecifications() != null && !product.getSpecifications().isEmpty() && product.getSpecifications().get(0) != null) {
                product.setSpecifications(Arrays.stream(product.getSpecifications().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                product.setSpecifications(Collections.emptyList());
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
            return "redirect:/admin/dashboard";
        } catch (IOException e) {
            logger.error("Error uploading image: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
            return "redirect:/admin/products/add";
        }
    }

    @DeleteMapping("/products/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProductApi(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            Product product = productService.findById(id);
            if (product == null) {
                logger.warn("Attempted to delete non-existent product with ID: {}", id);
                response.put("status", "error");
                response.put("message", "Product not found.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if (product.getImageId() != null && !product.getImageId().isEmpty()) {
                imageService.deleteImage(product.getImageId());
                logger.info("Associated image with ID {} deleted for product {}.", product.getImageId(), id);
            }

            productService.deleteById(id);
            logger.info("Product with ID {} deleted successfully via API.", id);
            response.put("status", "success");
            response.put("message", "Product deleted successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting product with ID {} via API: {}", id, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to delete product: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products/view/{id}")
    public String viewProduct(@PathVariable String id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            logger.warn("Product with id {} not found for view", id);
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("product", product);

        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            Category category = categoryService.findById(product.getCategory());
            if (category != null) {
                model.addAttribute("categoryName", category.getName());
            } else {
                logger.warn("Category with ID {} not found for product {}", product.getCategory(), product.getId());
                model.addAttribute("categoryName", "Unknown Category");
            }
        } else {
            model.addAttribute("categoryName", "N/A");
        }

        if (product.getImageId() != null && !product.getImageId().isEmpty()) {
            Image image = imageService.getImage(product.getImageId());
            if (image != null) {
                model.addAttribute("productImageUrl", "/image/" + image.getId());
            } else {
                logger.warn("Image with ID {} not found for product {}", product.getImageId(), product.getId());
            }
        }

        addImageAttributes(model);
        return "admin/view-product";
    }

    @GetMapping("/products/update/{id}")
    public String updateProductForm(@PathVariable String id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            logger.warn("Product with id {} not found", id);
            return "redirect:/admin/dashboard";
        }
        product.setSizes(product.getSizes() != null ? Collections.singletonList(String.join(",", product.getSizes())) : Collections.emptyList());
        product.setColors(product.getColors() != null ? Collections.singletonList(String.join(",", product.getColors())) : Collections.emptyList());
        product.setSpecifications(product.getSpecifications() != null ? Collections.singletonList(String.join(",", product.getSpecifications())) : Collections.emptyList());

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        addImageAttributes(model);
        return "admin/update-product";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable String id,
                                @ModelAttribute Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.findById(id);
            if (existingProduct == null) {
                logger.warn("Product with id {} not found for update", id);
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/admin/dashboard";
            }

            existingProduct.setName(product.getName());
            existingProduct.setCategory(product.getCategory());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStockQuantity(product.getStockQuantity());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setBrand(product.getBrand());
            existingProduct.setRating(product.getRating());
            existingProduct.setReviewCount(product.getReviewCount());

            if (imageFile != null && !imageFile.isEmpty()) {
                Image savedImage = imageService.storeImage(imageFile);
                existingProduct.setImageId(savedImage.getId());
            }

            if (product.getSizes() != null && !product.getSizes().isEmpty() && product.getSizes().get(0) != null) {
                existingProduct.setSizes(Arrays.stream(product.getSizes().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                existingProduct.setSizes(Collections.emptyList());
            }

            if (product.getColors() != null && !product.getColors().isEmpty() && product.getColors().get(0) != null) {
                existingProduct.setColors(Arrays.stream(product.getColors().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                existingProduct.setColors(Collections.emptyList());
            }

            if (product.getSpecifications() != null && !product.getSpecifications().isEmpty() && product.getSpecifications().get(0) != null) {
                existingProduct.setSpecifications(Arrays.stream(product.getSpecifications().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                existingProduct.setSpecifications(Collections.emptyList());
            }

            productService.save(existingProduct);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
            return "redirect:/admin/dashboard";
        } catch (IOException e) {
            logger.error("Error uploading image during update: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
            return "redirect:/admin/products/update/" + id;
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        addImageAttributes(model);
        return "admin/orders";
    }

    @GetMapping("/orders/view/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewOrder(@PathVariable String id, Model model) {
        OrderDTO order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        addImageAttributes(model);
        return "admin/order-view";
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewSettings(Model model) {
        String currentTheme = "light"; // Default theme
        model.addAttribute("currentTheme", currentTheme);
        addImageAttributes(model);
        return "admin/settings";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        addImageAttributes(model);
        return "admin/users";
    }

    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        model.addAttribute("logoImage", logoImage != null ? logoImage : createEmptyImage("apple-icon.png"));
        model.addAttribute("faviconImage", faviconImage != null ? faviconImage : createEmptyImage("favicon.ico"));
        logger.debug("Added image attributes for admin page");
    }

    private Image createEmptyImage(String name) {
        logger.warn("Image not found for name: {}", name);
        Image image = new Image();
        image.setName(name);
        image.setContentType("image/png");
        image.setData(new byte[0]);
        return image;
    }

    @GetMapping("/users/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUser(@PathVariable String id, Model model) {
        User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        addImageAttributes(model);
        return "admin/edit-user";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable("id") String id, @Valid @ModelAttribute("user") UserEditDTO userEditDTO,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Please correct the errors in the form.");
            return "admin/edit-users";
        }
        try {
            userService.updateUser(id, userEditDTO);
            model.addAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update user: " + e.getMessage());
            return "admin/edit-users";
        }
        return "redirect:/admin/users?success=User+updated+successfully";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") String id, Model model) {
        try {
            userService.delete(id);
            return "redirect:/admin/users?success=User+deleted+successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete user: " + e.getMessage());
            return "admin/users";
        }
    }

    // New category management methods
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        addImageAttributes(model);
        return "admin/categories";
    }

    @GetMapping("/categories/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCategoryForm(@PathVariable String id, Model model) {
        Category category = categoryService.findById(id);
        if (category == null) {
            logger.error("Category not found for ID: {}", id);
            model.addAttribute("error", "Category not found");
            return "admin/categories";
        }
        model.addAttribute("category", category);
        addImageAttributes(model);
        return "admin/update-category";
    }

    @PostMapping("/categories/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCategory(@PathVariable String id,
                                 @RequestParam("name") String name,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 RedirectAttributes redirectAttributes) throws IOException {
        Category existingCategory = categoryService.findById(id);
        if (existingCategory == null) {
            logger.error("Category not found for ID: {}", id);
            redirectAttributes.addFlashAttribute("error", "Category not found");
            return "redirect:/admin/categories";
        }
        existingCategory.setName(name);
        if (imageFile != null && !imageFile.isEmpty()) {
            logger.info("Uploading image for category: {}", id);
            Image savedImage = imageService.storeImage(imageFile);
            existingCategory.setImage(savedImage.getId());
            logger.info("Image saved with ID: {}", savedImage.getId());
        }
        categoryService.save(existingCategory);
        redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
        return "redirect:/admin/categories";
    }
}