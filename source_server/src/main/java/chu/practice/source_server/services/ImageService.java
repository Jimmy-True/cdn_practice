package chu.practice.source_server.services;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import chu.practice.source_server.entities.Image;
import chu.practice.source_server.repos.ImageRepository;

@Service
@DependsOn("redisTemplate")
public class ImageService {
    private final Logger logger = LoggerFactory.getLogger(ImageService.class);

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    public void saveImage(String name, MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(name);
        image.setData(file.getBytes());
        try {
            imageRepository.save(image);
        } catch (Exception e) {
            logger.error(name + " failed to save with error: ", e);
        }
    }

    public byte[] getImage(String name) {
        // Check Redis cache first
        byte[] cachedImage = redisTemplate.opsForValue().get(name);
        if (cachedImage != null) {
            logger.info("Image {} retrieved from Redis cache!", name);
            return cachedImage;
        }

        // If not cached, retrieve from MongoDB and store in Redis cache
        Image image = imageRepository.findById(name).orElse(null);
        byte[] imageData = image != null ? image.getData() : null;
        if (imageData != null) {
            redisTemplate.opsForValue().set(name, imageData, 3600, java.util.concurrent.TimeUnit.SECONDS);
            logger.info("Image {} retrieved from MongoDB!", name);
        } else {
            logger.warn("Image {} not found!", name);
        }
        return imageData;
    }

    public void deleteImage(String name) {
        imageRepository.deleteById(name);
    }
}
