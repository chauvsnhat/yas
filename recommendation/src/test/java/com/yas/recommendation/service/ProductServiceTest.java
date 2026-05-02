package com.yas.recommendation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.yas.recommendation.configuration.RecommendationConfig;
import com.yas.recommendation.viewmodel.ProductDetailVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import java.net.URI;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RecommendationConfig config;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(restClient, config);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProductDetail_shouldReturnProductDetailVm() {
        Long productId = 1L;
        ProductDetailVm expectedVm = new ProductDetailVm(
                productId, "Test Product", "Short", "Long", "Spec", "SKU", "GTIN", "slug",
                true, true, true, true, true, 10.0, 1L, Collections.emptyList(),
                "Meta", "Key", "Desc", 1L, "Brand", Collections.emptyList(),
                Collections.emptyList(), null, Collections.emptyList()
        );

        when(config.getApiUrl()).thenReturn("http://product-service");
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(expectedVm));

        ProductDetailVm result = productService.getProductDetail(productId);

        assertNotNull(result);
        assertEquals(productId, result.id());
    }
}
