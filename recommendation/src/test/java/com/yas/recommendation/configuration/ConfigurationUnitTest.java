package com.yas.recommendation.configuration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

class ConfigurationUnitTest {

    @Test
    void appConfig_shouldCreateObjectMapper() {
        AppConfig appConfig = new AppConfig();
        ObjectMapper objectMapper = appConfig.objectMapper();
        assertNotNull(objectMapper);
    }

    @Test
    void embeddingSearchConfiguration_shouldStoreValues() {
        EmbeddingSearchConfiguration config = new EmbeddingSearchConfiguration(0.7, 10);
        assertEquals(0.7, config.similarityThreshold());
        assertEquals(10, config.topK());
    }

    @Test
    void recommendationConfig_shouldStoreValues() {
        RecommendationConfig config = new RecommendationConfig("http://product-service");
        assertEquals("http://product-service", config.productServiceUrl());
    }

    @Test
    void vectorStoreProperties_shouldStoreValues() {
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setTableName("test_table");
        assertEquals("test_table", properties.getTableName());
    }
}
