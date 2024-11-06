package chu.practice.source_server.services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Listens on Kafka topic "record-change-topic" and deletes the corresponding
 * record from Redis to ensure data consistency when MongoDB changes.
 */
@Component
public class RedisDeletionConsumer {
    Logger logger = LoggerFactory.getLogger(RedisDeletionConsumer.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.max-retries}")
    private final int maxRetries = 3;

    public RedisDeletionConsumer(KafkaTemplate<String, String> kafkaTemplate,
            RedisTemplate<String, String> redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "record-change-topic")
    public void handleRecordChange(ConsumerRecord<String, String> record) {
        String recordId = record.value();
        boolean success = tryDeleteFromRedis(recordId, 0);

        if (!success) {
            logger.warn("Failed to delete record {} from Redis!", recordId);
            // Send message to Kafka topic "record-delete-failed-topic", consumers will be
            // implemented in the future
            kafkaTemplate.send("record-delete-failed-topic", recordId);
        }
    }

    private boolean tryDeleteFromRedis(String recordId, int retryCount) {
        try {
            String value = redisTemplate.opsForValue().get(recordId);
            if (value != null) {
                redisTemplate.delete(recordId);
            }
            return true;
        } catch (Exception e) {
            if (retryCount < maxRetries) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                return tryDeleteFromRedis(recordId, retryCount + 1);
            }
            return false;
        }
    }
}
