package com.example.controller;

import com.example.dto.OrderDTO;
import com.example.model.Product;
import com.example.service.OrderService;
import com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/analytics")
public class AnalyticsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String getAnalytics() {
        return "admin/analytics";
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAnalyticsData() {
        // Fetch products
        List<Product> products = productService.findAll(null, null).getContent();

        // Fetch order status counts, normalize to uppercase
        List<OrderDTO> orders = orderService.getAllOrders();
        Map<String, Long> orderStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus().toUpperCase(), // Normalize to uppercase
                        Collectors.counting()
                ));

        // Prepare response
        Map<String, Object> data = new HashMap<>();
        data.put("products", products);
        data.put("orderStatus", orderStatus);
        return ResponseEntity.ok(data);
    }
}