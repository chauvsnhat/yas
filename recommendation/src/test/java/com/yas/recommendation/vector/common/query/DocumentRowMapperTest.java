package com.yas.recommendation.vector.common.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class DocumentRowMapperTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResultSet resultSet;

    private DocumentRowMapper documentRowMapper;

    @BeforeEach
    void setUp() {
        documentRowMapper = new DocumentRowMapper(objectMapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapRow_shouldReturnDocument() throws SQLException, com.fasterxml.jackson.core.JsonProcessingException {
        String id = "test-id";
        String content = "test-content";
        String metadataJson = "{\"key\":\"value\"}";
        Double similarity = 0.95;

        when(resultSet.getString("id")).thenReturn(id);
        when(resultSet.getString("content")).thenReturn(content);
        when(resultSet.getString("metadata")).thenReturn(metadataJson);
        when(resultSet.getDouble("similarity")).thenReturn(similarity);
        
        Map<String, Object> metadataMap = Map.of("key", "value");
        when(objectMapper.readValue(metadataJson, Map.class)).thenReturn(metadataMap);

        Document result = documentRowMapper.mapRow(resultSet, 1);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(content, result.getContent());
        assertEquals(0.95, result.getMetadata().get("similarity"));
        assertEquals("value", result.getMetadata().get("key"));
    }
}
