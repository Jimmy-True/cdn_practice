package chu.practice.source_server.controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import chu.practice.source_server.services.ImageService;

@RestController
@RequestMapping("/images")
public class ImageController {
    Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {
        // Verify that the file is an image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warn("File {} is not an image!", name);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is not an image");
        }
        try {
            imageService.saveImage(name, file);
            logger.info("Image {} uploaded successfully!", name);
            return ResponseEntity.ok("Image uploaded successfully");
        } catch (IOException e) {
            logger.error("Failed to upload image {} with error: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }

    @PostMapping("/delete/{name}")
    public ResponseEntity<String> deleteImage(@PathVariable String name) {
        try {
            imageService.deleteImage(name);
        } catch (Exception e) {
            logger.error("Failed to delete image {} with error: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete image");
        }
        logger.info("Image {} deleted successfully!", name);
        return ResponseEntity.ok("Image deleted successfully");
    }

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> getImage(@PathVariable String name) {
        byte[] imageData = imageService.getImage(name);
        if (imageData == null) {
            logger.warn("Image {} not found!", name);
            return ResponseEntity.notFound().build();
        }
        logger.info("Image {} retrieved successfully!", name);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(imageData);
    }
}
