package chu.practice.source_server.services;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;

import jakarta.annotation.PostConstruct;

/**
 * Listens to changes in the MongoDB collection and sends them to Kafka.
 */
@Component
public class MongoChangeStreamListener {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MongoTemplate mongoTemplate;

    public MongoChangeStreamListener(KafkaTemplate<String, String> kafkaTemplate, MongoTemplate mongoTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void init() {
        MongoCollection<Document> collection = mongoTemplate.getCollection("images");

        collection.watch().forEach(change -> {
            String recordId = change.getDocumentKey().get("name").toString();
            kafkaTemplate.send("record-change-topic", recordId);
        });
    }
}
