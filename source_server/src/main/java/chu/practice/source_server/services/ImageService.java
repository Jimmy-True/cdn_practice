package chu.practice.source_server.services;

import java.io.IOException;

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
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    public void saveImage(String name, MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(name);
        image.setData(file.getBytes());
        imageRepository.save(image);

        // Cache the image in Redis if it's not already cached
        redisTemplate.opsForValue().set(name, file.getBytes());
    }

    public byte[] getImage(String name) {
        // Check Redis cache first
        byte[] cachedImage = redisTemplate.opsForValue().get(name);
        if (cachedImage != null) {
            return cachedImage;
        }

        // If not cached, retrieve from MongoDB
        Image image = imageRepository.findById(name).orElse(null);
        return image != null ? image.getData() : null;
    }

    public void deleteImage(String name) {
        redisTemplate.delete(name);
        imageRepository.deleteById(name);
    }
}
