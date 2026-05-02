package com.yas.recommendation.vector.common.query;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.recommendation.configuration.EmbeddingSearchConfiguration;
import com.yas.recommendation.vector.product.document.ProductDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class JdbcVectorServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmbeddingSearchConfiguration embeddingSearchConfiguration;

    @Mock
    private PreparedStatement preparedStatement;

    private JdbcVectorService jdbcVectorService;

    @BeforeEach
    void setUp() {
        jdbcVectorService = new JdbcVectorService(jdbcTemplate, objectMapper, embeddingSearchConfiguration);
        ReflectionTestUtils.setField(jdbcVectorService, "vectorTableName", "vector_store");
    }

    @Test
    @SuppressWarnings("unchecked")
    void similarityProduct_shouldExecuteQuery() throws SQLException {
        Long productId = 1L;
        Class<ProductDocument> docType = ProductDocument.class;
        List<Document> expectedDocs = List.of(new Document("test content"));

        when(embeddingSearchConfiguration.topK()).thenReturn(10);
        when(embeddingSearchConfiguration.similarityThreshold()).thenReturn(0.7);
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(expectedDocs);

        List<Document> result = jdbcVectorService.similarityProduct(productId, docType);

        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Capture and test PreparedStatementSetter lambda
        ArgumentCaptor<PreparedStatementSetter> pssCaptor = ArgumentCaptor.forClass(PreparedStatementSetter.class);
        verify(jdbcTemplate).query(contains("SELECT"), pssCaptor.capture(), any(RowMapper.class));
        
        PreparedStatementSetter pss = pssCaptor.getValue();
        pss.setValues(preparedStatement);
        
        verify(preparedStatement, times(4)).setObject(anyInt(), any());
    }

    @Test
    void similarityProduct_withNullDocType_shouldUseDefaultPrefix() {
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(List.of());
        
        jdbcVectorService.similarityProduct(1L, null);
        
        verify(jdbcTemplate).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
    }
}
