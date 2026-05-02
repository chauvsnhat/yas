package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeTemplate;
import com.yas.product.model.attribute.ProductTemplate;
import com.yas.product.repository.ProductAttributeRepository;
import com.yas.product.repository.ProductAttributeTemplateRepository;
import com.yas.product.repository.ProductTemplateRepository;
import com.yas.product.viewmodel.producttemplate.ProductAttributeTemplatePostVm;
import com.yas.product.viewmodel.producttemplate.ProductTemplatePostVm;
import com.yas.product.viewmodel.producttemplate.ProductTemplateVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductTemplateServiceTest {

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private ProductAttributeTemplateRepository productAttributeTemplateRepository;

    @Mock
    private ProductAttributeRepository productAttributeRepository;

    @InjectMocks
    private ProductTemplateService productTemplateService;

    @Test
    void getProductTemplate_Success() {
        ProductTemplate productTemplate = new ProductTemplate();
        productTemplate.setId(1L);
        productTemplate.setName("Template 1");
        when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(productTemplate));
        when(productAttributeTemplateRepository.findAllByProductTemplateId(1L)).thenReturn(List.of());

        ProductTemplateVm result = productTemplateService.getProductTemplate(1L);

        assertEquals("Template 1", result.name());
    }

    @Test
    void getProductTemplate_NotFound_ThrowsException() {
        when(productTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productTemplateService.getProductTemplate(1L));
    }

    @Test
    void saveProductTemplate_Success() {
        ProductTemplatePostVm postVm = new ProductTemplatePostVm("New Template", List.of(new ProductAttributeTemplatePostVm(1L, 1)));
        ProductAttribute attribute = new ProductAttribute();
        attribute.setId(1L);
        
        when(productTemplateRepository.findExistedName("New Template", null)).thenReturn(null);
        when(productAttributeRepository.findAllById(any())).thenReturn(List.of(attribute));
        when(productTemplateRepository.save(any())).thenAnswer(invocation -> {
            ProductTemplate pt = invocation.getArgument(0);
            pt.setId(1L);
            return pt;
        });
        when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(new ProductTemplate()));

        productTemplateService.saveProductTemplate(postVm);

        verify(productTemplateRepository).save(any());
    }

    @Test
    void updateProductTemplate_Success() {
        ProductTemplate productTemplate = new ProductTemplate();
        productTemplate.setId(1L);
        productTemplate.setName("Old Name");
        
        ProductTemplatePostVm postVm = new ProductTemplatePostVm("Updated Name", List.of());
        
        when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(productTemplate));
        when(productAttributeTemplateRepository.findAllByProductTemplateId(1L)).thenReturn(List.of());

        productTemplateService.updateProductTemplate(1L, postVm);

        assertEquals("Updated Name", productTemplate.getName());
        verify(productTemplateRepository).save(productTemplate);
    }
}
