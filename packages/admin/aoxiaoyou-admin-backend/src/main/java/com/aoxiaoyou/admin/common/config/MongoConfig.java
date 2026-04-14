package com.aoxiaoyou.admin.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;

@Slf4j
@Configuration
public class MongoConfig {

    @Bean
    public CommandLineRunner mongoInitializer(MongoTemplate mongoTemplate) {
        return args -> {
            List<String> collections = List.of(
                    "storyline_documents",
                    "poi_rule_documents",
                    "ai_policy_documents",
                    "user_event_logs",
                    "campaign_documents",
                    "indoor_anchor_documents"
            );

            try {
                for (String collection : collections) {
                    if (!mongoTemplate.collectionExists(collection)) {
                        mongoTemplate.createCollection(collection);
                        log.info("Created Mongo collection: {}", collection);
                    }
                }

                if (mongoTemplate.findById("mongo-bootstrap", org.bson.Document.class, "system_meta") == null) {
                    if (!mongoTemplate.collectionExists("system_meta")) {
                        mongoTemplate.createCollection("system_meta");
                    }
                    org.bson.Document doc = new org.bson.Document();
                    doc.put("_id", "mongo-bootstrap");
                    doc.put("project", "trip-of-macau-admin");
                    doc.put("description", "Local MongoDB bootstrap for admin backend document collections.");
                    doc.put("createdAt", Instant.now().toString());
                    doc.put("collections", collections);
                    mongoTemplate.save(doc, "system_meta");
                }
            } catch (Exception ex) {
                log.warn("Skipping Mongo bootstrap because the document store is unavailable or credentials are invalid. Admin HTTP services will continue to start.", ex);
            }
        };
    }
}
