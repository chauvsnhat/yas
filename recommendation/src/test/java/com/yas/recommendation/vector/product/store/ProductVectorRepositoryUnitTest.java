package com.yas.recommendation.vector.product.store;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.recommendation.configuration.EmbeddingSearchConfiguration;
import com.yas.recommendation.service.ProductService;
import com.yas.recommendation.viewmodel.ProductDetailVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ProductVectorRepositoryUnitTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ProductService productService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmbeddingSearchConfiguration embeddingSearchConfiguration;

    private ProductVectorRepository productVectorRepository;

    @BeforeEach
    void setUp() {
        productVectorRepository = new ProductVectorRepository(vectorStore, productService);
        ReflectionTestUtils.setField(productVectorRepository, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(productVectorRepository, "embeddingSearchConfiguration", embeddingSearchConfiguration);
    }

    @Test
    void getEntity_shouldCallProductService() {
        Long productId = 1L;
        ProductDetailVm expectedVm = createProductDetailVm(productId);
        when(productService.getProductDetail(productId)).thenReturn(expectedVm);

        ProductDetailVm result = productVectorRepository.getEntity(productId);

        assertNotNull(result);
        assertEquals(productId, result.id());
        verify(productService).getProductDetail(productId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void add_shouldFetchFormatAndSave() {
        Long productId = 1L;
        ProductDetailVm product = createProductDetailVm(productId);
        Map<String, Object> productMap = new HashMap<>(Map.of("id", productId, "name", "Test"));

        when(productService.getProductDetail(productId)).thenReturn(product);
        when(objectMapper.convertValue(product, Map.class)).thenReturn(productMap);

        productVectorRepository.add(productId);

        ArgumentCaptor<List<Document>> docsCaptor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(docsCaptor.capture());
        
        List<Document> savedDocs = docsCaptor.getValue();
        assertEquals(1, savedDocs.size());
        assertEquals("Test", savedDocs.get(0).getMetadata().get("name"));
    }

    @Test
    void delete_shouldCallVectorStoreDelete() {
        Long productId = 1L;
        
        productVectorRepository.delete(productId);

        verify(vectorStore).delete(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void update_shouldDeleteAndAdd() {
        Long productId = 1L;
        ProductDetailVm product = createProductDetailVm(productId);
        Map<String, Object> productMap = new HashMap<>(Map.of("id", productId, "name", "Test"));

        when(productService.getProductDetail(productId)).thenReturn(product);
        when(objectMapper.convertValue(product, Map.class)).thenReturn(productMap);

        productVectorRepository.update(productId);

        verify(vectorStore).delete(anyList());
        verify(vectorStore).add(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_shouldPerformSimilaritySearch() {
        Long productId = 1L;
        ProductDetailVm product = createProductDetailVm(productId);
        Map<String, Object> productMap = new HashMap<>(Map.of("id", productId, "name", "Test"));
        
        when(productService.getProductDetail(productId)).thenReturn(product);
        when(objectMapper.convertValue(product, Map.class)).thenReturn(productMap);
        when(embeddingSearchConfiguration.topK()).thenReturn(5);
        when(embeddingSearchConfiguration.similarityThreshold()).thenReturn(0.7);
        
        Document doc = new Document("result", Map.of("id", 2L));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        var results = productVectorRepository.search(productId);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    private ProductDetailVm createProductDetailVm(Long id) {
        return new ProductDetailVm(
                id, "Test", "Short", "Long", "Spec", "SKU", "GTIN", "slug",
                true, true, true, true, true, 10.0, 1L, Collections.emptyList(),
                "Meta", "Key", "Desc", 1L, "Brand", Collections.emptyList(),
                Collections.emptyList(), null, Collections.emptyList()
        );
    }
}
