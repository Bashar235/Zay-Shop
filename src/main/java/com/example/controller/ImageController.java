package com.example.controller;

import com.example.model.Image;
import com.example.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        logger.debug("Requesting image ID: {}", id);
        Image image = imageService.getImage(id);
        if (image != null && image.getData() != null && image.getData().length > 0) {
            logger.info("Serving image ID: {} (name: {})", id, image.getName());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .body(image.getData());
        }

        logger.warn("Image not found or empty for ID: {}", id);
        // TODO: Optionally serve a default image from static resources
         Image defaultImage = imageService.findByName("default-image.png");
         if (defaultImage != null) {
             return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(defaultImage.getContentType()))
                     .body(defaultImage.getData());
        }
        return ResponseEntity.notFound().build();
    }
}