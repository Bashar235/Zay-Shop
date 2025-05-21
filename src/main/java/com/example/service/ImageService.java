package com.example.service;

import com.example.model.Image;
import com.example.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Image storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.error("Cannot store empty or null file");
            throw new IOException("Invalid file");
        }
        String filename = file.getOriginalFilename();
        // Check for existing image to prevent duplicates
        Optional<Image> existingImage = imageRepository.findByName(filename);
        if (existingImage.isPresent()) {
            logger.info("Image with name {} already exists with ID: {}", filename, existingImage.get().getId());
            return existingImage.get();
        }
        Image image = new Image();
        image.setName(filename);
        image.setContentType(file.getContentType());
        image.setData(file.getBytes());
        Image savedImage = imageRepository.save(image);
        logger.info("Stored image: {} with ID: {}", filename, savedImage.getId());
        return savedImage;
    }

    public Image getImage(String id) {
        if (id == null) {
            logger.warn("Image ID is null");
            return null;
        }
        Image image = imageRepository.findById(id).orElse(null);
        if (image == null) {
            logger.warn("Image not found for ID: {}", id);
        }
        return image;
    }

    public Image findByName(String name) {
        if (name == null) {
            logger.warn("Image name is null");
            return null;
        }
        Image image = imageRepository.findByName(name).orElse(null);
        if (image == null) {
            logger.warn("Image not found for name: {}", name);
        }
        return image;
    }


    /**
     * Deletes an image by its ID.
     *
     * @param id The ID of the image to delete.
     * @return true if the image was found and deleted, false otherwise.
     */
    public boolean deleteImage(String id) {
        if (id == null) {
            logger.warn("Attempted to delete image with null ID.");
            return false;
        }
        Optional<Image> imageOptional = imageRepository.findById(id);
        if (imageOptional.isPresent()) {
            imageRepository.deleteById(id);
            logger.info("Image with ID: {} deleted successfully.", id);
            return true;
        } else {
            logger.warn("Image with ID: {} not found for deletion.", id);
            return false;
        }
    }
}