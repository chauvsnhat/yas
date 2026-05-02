package com.yas.recommendation.vector.product.formatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.recommendation.viewmodel.CategoryVm;
import com.yas.recommendation.viewmodel.ProductAttributeValueVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ProductDocumentFormatterTest {

    @Mock
    private ObjectMapper objectMapper;

    private ProductDocumentFormatter productDocumentFormatter;

    @BeforeEach
    void setUp() {
        productDocumentFormatter = new ProductDocumentFormatter();
    }

    @Test
    @SuppressWarnings("unchecked")
    void format_shouldReturnFormattedString() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "iPhone");
        entityMap.put("brandName", "Apple");
        entityMap.put("price", 1000.0);
        
        List<Map<String, Object>> categories = List.of(Map.of("name", "Smartphone"));
        entityMap.put("categories", categories);
        
        List<Map<String, Object>> attributes = List.of(Map.of("nameProductAttribute", "Color", "value", "Black"));
        entityMap.put("attributeValues", attributes);

        String template = "{name}| {brandName}| Price: {price}| {categories}| {attributeValues}";

        when(objectMapper.convertValue(any(), eq(CategoryVm.class)))
                .thenReturn(new CategoryVm(1L, "Smartphone", "desc", "slug", "key", "desc", (short)1, true));
        when(objectMapper.convertValue(any(), eq(ProductAttributeValueVm.class)))
                .thenReturn(new ProductAttributeValueVm(1L, "Color", "Black"));

        String result = productDocumentFormatter.format(entityMap, template, objectMapper);

        assertNotNull(result);
        assertTrue(result.contains("iPhone"));
        assertTrue(result.contains("Apple"));
        assertTrue(result.contains("Price: 1000.0"));
        assertTrue(result.contains("Smartphone"));
        assertTrue(result.contains("Color: Black"));
    }
}
