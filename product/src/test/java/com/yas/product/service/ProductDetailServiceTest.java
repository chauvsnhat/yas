package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.ImageVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import com.yas.product.viewmodel.product.ProductVariationGetVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    private Product product;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setPublished(true);
        product.setHasOptions(true);
        
        Product childProduct = new Product();
        childProduct.setId(2L);
        childProduct.setName("Child Product");
        childProduct.setSlug("child-product");
        childProduct.setPublished(true);
        product.setProducts(List.of(childProduct));

        noFileMediaVm = new NoFileMediaVm(1L, "caption", "fileName", "mediaType", "url");
    }

    @Test
    void getProductDetailById_WithAttributes_Success() {
        com.yas.product.model.attribute.ProductAttribute attribute = new com.yas.product.model.attribute.ProductAttribute();
        attribute.setName("Color");
        
        com.yas.product.model.attribute.ProductAttributeValue attributeValue = new com.yas.product.model.attribute.ProductAttributeValue();
        attributeValue.setId(1L);
        attributeValue.setProductAttribute(attribute);
        attributeValue.setValue("Blue");
        
        product.setAttributeValues(List.of(attributeValue));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNotNull(result);
        assertEquals(1, result.getAttributeValues().size());
        assertEquals("Color", result.getAttributeValues().get(0).nameProductAttribute());
        assertEquals("Blue", result.getAttributeValues().get(0).value());
    }

    @Test
    void getProductDetailById_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_NotPublished_ThrowsException() {
        product.setPublished(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_FullInfo_Success() {
        // Setup Brand
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Test Brand");
        product.setBrand(brand);

        // Setup Categories
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        ProductCategory productCategory = ProductCategory.builder()
                .category(category)
                .product(product)
                .build();
        product.setProductCategories(List.of(productCategory));

        // Setup Attributes
        product.setAttributeValues(List.of());

        // Setup Media
        product.setThumbnailMediaId(1L);
        ProductImage productImage = ProductImage.builder()
                .imageId(2L)
                .product(product)
                .build();
        product.setProductImages(List.of(productImage));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "cap", "file", "type", "url2"));

        // Setup Variations
        Product childProduct = product.getProducts().get(0);
        childProduct.setPublished(true);
        childProduct.setThumbnailMediaId(3L);
        when(mediaService.getMedia(3L)).thenReturn(new NoFileMediaVm(3L, "cap3", "file3", "type3", "url3"));

        ProductOptionCombination combination = new ProductOptionCombination();
        ProductOption option = new ProductOption();
        option.setId(1L);
        combination.setProductOption(option);
        combination.setValue("red");
        when(productOptionCombinationRepository.findAllByProduct(childProduct)).thenReturn(List.of(combination));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Test Brand", result.getBrandName());
        assertEquals(1, result.getCategories().size());
        assertEquals("Test Category", result.getCategories().get(0).getName());
        assertEquals("url", result.getThumbnail().url());
        assertEquals(1, result.getProductImages().size());
        assertEquals("url2", result.getProductImages().get(0).url());
        assertEquals(1, result.getVariations().size());
        assertEquals("url3", result.getVariations().get(0).thumbnail().url());
    }

    @Test
    void getProductDetailById_NoOptions_Success() {
        product.setHasOptions(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNotNull(result);
        assertEquals(0, result.getVariations().size());
    }

    @Test
    void getProductDetailById_NullBrandAndCategories_Success() {
        product.setBrand(null);
        product.setProductCategories(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNotNull(result);
        assertEquals(null, result.getBrandName());
        assertEquals(0, result.getCategories().size());
    }
}
