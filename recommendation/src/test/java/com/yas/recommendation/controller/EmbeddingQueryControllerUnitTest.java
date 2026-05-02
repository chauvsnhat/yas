package com.yas.recommendation.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.yas.recommendation.vector.common.query.VectorQuery;
import com.yas.recommendation.vector.product.document.ProductDocument;
import com.yas.recommendation.viewmodel.RelatedProductVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class EmbeddingQueryControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private VectorQuery<ProductDocument, RelatedProductVm> relatedProductSearch;

    private EmbeddingQueryController embeddingQueryController;

    @BeforeEach
    void setUp() {
        embeddingQueryController = new EmbeddingQueryController(relatedProductSearch);
        mockMvc = MockMvcBuilders.standaloneSetup(embeddingQueryController).build();
    }

    @Test
    void searchProduct_shouldReturnDocuments() throws Exception {
        Long productId = 1L;
        RelatedProductVm resultVm = new RelatedProductVm();
        resultVm.setProductId(2L);
        resultVm.setName("Similar Product");
        
        when(relatedProductSearch.similaritySearch(productId)).thenReturn(List.of(resultVm));

        mockMvc.perform(get("/embedding/product/{id}/similarity", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Similar Product"));
        
        verify(relatedProductSearch).similaritySearch(productId);
    }
}
