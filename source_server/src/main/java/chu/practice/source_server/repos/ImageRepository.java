package chu.practice.source_server.repos;

import org.springframework.data.mongodb.repository.MongoRepository;

import chu.practice.source_server.entities.Image;

public interface ImageRepository extends MongoRepository<Image, String> {

}
