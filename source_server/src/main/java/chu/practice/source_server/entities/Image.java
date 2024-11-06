package chu.practice.source_server.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "images")
@Data
public class Image {
    @Id
    private String name;
    private byte[] data;
}
