package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductRelated;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.utils.Constants;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductListGetFromCategoryVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductOptionValueDisplay;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductProperties;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductThumbnailVm;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import com.yas.product.model.ProductOption;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import static org.mockito.Mockito.atLeastOnce;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionValueRepository productOptionValueRepository;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Brand brand;
    private Category category;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Test Brand");
        brand.setSlug("test-brand");

        category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        category.setSlug("test-category");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setSku("sku");
        product.setGtin("gtin");
        product.setBrand(brand);
        product.setThumbnailMediaId(1L);
        product.setProductImages(new ArrayList<>());
        product.setProductCategories(new ArrayList<>());
        
        noFileMediaVm = new NoFileMediaVm(1L, "caption", "fileName", "mediaType", "url");
    }

    // --- Tests for getProductById ---
    @Test
    void getProductById_Success() {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory(category);
        productCategory.setProduct(product);
        product.getProductCategories().add(productCategory);

        ProductImage productImage = new ProductImage();
        productImage.setImageId(2L);
        productImage.setProduct(product);
        product.getProductImages().add(productImage);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "cap", "file", "img", "url2"));

        ProductDetailVm result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertEquals("Test Product", result.name());
        assertEquals(1L, result.brandId());
        assertEquals(1, result.categories().size());
        assertEquals("Test Category", result.categories().get(0).getName());
        assertEquals("url", result.thumbnailMedia().url());
        assertEquals(1, result.productImageMedias().size());
        assertEquals("url2", result.productImageMedias().get(0).url());
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> productService.getProductById(99L));
        assertEquals(String.format("Product %s is not found", 99L), exception.getMessage());
    }

    // --- Tests for getLatestProducts ---
    @Test
    void getLatestProducts_CountLessThanOne_ReturnsEmpty() {
        List<ProductListVm> result = productService.getLatestProducts(0);
        assertThat(result).isEmpty();
        
        result = productService.getLatestProducts(-1);
        assertThat(result).isEmpty();
    }

    @Test
    void getLatestProducts_Success() {
        List<Product> products = List.of(product);
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(products);

        List<ProductListVm> result = productService.getLatestProducts(1);
        
        assertThat(result).hasSize(1);
        assertEquals("Test Product", result.get(0).name());
    }

    // --- Tests for getProductsByBrand ---
    @Test
    void getProductsByBrand_BrandNotFound_ThrowsException() {
        when(brandRepository.findBySlug("invalid")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("invalid"));
        assertEquals(String.format("Brand %s is not found", "invalid"), exception.getMessage());
    }

    @Test
    void getProductsByBrand_Success() {
        when(brandRepository.findBySlug("test-brand")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        List<ProductThumbnailVm> result = productService.getProductsByBrand("test-brand");

        assertThat(result).hasSize(1);
        assertEquals("Test Product", result.get(0).name());
        assertEquals("url", result.get(0).thumbnailUrl());
    }

    // --- Tests for getProductsFromCategory ---
    @Test
    void getProductsFromCategory_CategoryNotFound_ThrowsException() {
        when(categoryRepository.findBySlug("invalid")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> productService.getProductsFromCategory(0, 10, "invalid"));
        assertEquals(String.format("Category %s is not found", "invalid"), exception.getMessage());
    }

    @Test
    void getProductsFromCategory_Success() {
        when(categoryRepository.findBySlug("test-category")).thenReturn(Optional.of(category));
        
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory(category);
        productCategory.setProduct(product);
        
        Page<ProductCategory> page = new PageImpl<>(List.of(productCategory));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), any(Category.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 10, "test-category");

        assertThat(result.productContent()).hasSize(1);
        assertEquals("Test Product", result.productContent().get(0).name());
        assertEquals("url", result.productContent().get(0).thumbnailUrl());
        assertEquals(0, result.pageNo());
        assertEquals(1, result.totalElements());
    }
    
    // --- Tests for createProduct ---
    @Test
    void createProduct_LengthLessThanWidth_ThrowsException() {
        ProductPostVm postVm = org.mockito.Mockito.mock(ProductPostVm.class);
        when(postVm.length()).thenReturn(5.0);
        when(postVm.width()).thenReturn(10.0);
        com.yas.commonlibrary.exception.BadRequestException exception = assertThrows(
            com.yas.commonlibrary.exception.BadRequestException.class, 
            () -> productService.createProduct(postVm));
        assertThat(exception.getMessage()).contains("length greater than width");
    }

    @Test
    void createProduct_SlugExists_ThrowsException() {
        ProductPostVm postVm = org.mockito.Mockito.mock(ProductPostVm.class);
        when(postVm.length()).thenReturn(10.0);
        when(postVm.width()).thenReturn(5.0);
        when(postVm.slug()).thenReturn("existing-slug");
        when(productRepository.findBySlugAndIsPublishedTrue("existing-slug")).thenReturn(Optional.of(product));
        
        DuplicatedException exception = assertThrows(
            DuplicatedException.class, 
            () -> productService.createProduct(postVm));
        assertThat(exception.getMessage()).contains("is already existed or is duplicated");
    }

    @Test
    void createProduct_Success() {
        ProductPostVm postVm = org.mockito.Mockito.mock(ProductPostVm.class);
        when(postVm.length()).thenReturn(10.0);
        when(postVm.width()).thenReturn(5.0);
        when(postVm.slug()).thenReturn("new-product");
        when(postVm.name()).thenReturn("New Product");
        when(postVm.sku()).thenReturn("new-sku");
        when(postVm.gtin()).thenReturn("new-gtin");
        when(postVm.brandId()).thenReturn(1L);
        when(postVm.categoryIds()).thenReturn(List.of(1L));
        when(productRepository.findBySlugAndIsPublishedTrue("new-product")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("new-gtin")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("new-sku")).thenReturn(Optional.empty());
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        
        Product savedProduct = new Product();
        savedProduct.setId(2L);
        savedProduct.setName("New Product");
        savedProduct.setSlug("new-product");
        savedProduct.setProductCategories(new ArrayList<>());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        com.yas.product.viewmodel.product.ProductGetDetailVm result = productService.createProduct(postVm);

        assertThat(result).isNotNull();
        assertEquals(2L, result.id());
        assertEquals("New Product", result.name());
    }

    // --- Tests for updateProduct ---
    @Test
    void updateProduct_NotFound_ThrowsException() {
        com.yas.product.viewmodel.product.ProductPutVm putVm = org.mockito.Mockito.mock(com.yas.product.viewmodel.product.ProductPutVm.class);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> productService.updateProduct(99L, putVm));
        assertEquals(String.format("Product %s is not found", 99L), exception.getMessage());
    }

    @Test
    void updateProduct_Success() {
        com.yas.product.viewmodel.product.ProductPutVm putVm = org.mockito.Mockito.mock(com.yas.product.viewmodel.product.ProductPutVm.class);
        when(putVm.length()).thenReturn(10.0);
        when(putVm.width()).thenReturn(5.0);
        when(putVm.slug()).thenReturn("test-product");
        when(putVm.name()).thenReturn("Updated Product");
        when(putVm.gtin()).thenReturn("gtin");
        when(putVm.sku()).thenReturn("sku");
        when(putVm.brandId()).thenReturn(1L);
        when(putVm.categoryIds()).thenReturn(List.of(1L));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(productRepository.findByGtinAndIsPublishedTrue("gtin")).thenReturn(Optional.of(product));
        when(productRepository.findBySkuAndIsPublishedTrue("sku")).thenReturn(Optional.of(product));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));

        com.yas.product.viewmodel.productoption.ProductOptionValuePutVm optionPutVm = org.mockito.Mockito.mock(com.yas.product.viewmodel.productoption.ProductOptionValuePutVm.class);
        when(putVm.productOptionValues()).thenReturn(List.of(optionPutVm));
        when(optionPutVm.productOptionId()).thenReturn(1L);

        com.yas.product.model.ProductOption option = new com.yas.product.model.ProductOption();
        option.setId(1L);
        when(productOptionRepository.findAllByIdIn(any())).thenReturn(List.of(option));
        
        when(putVm.variations()).thenReturn(List.of());

        productService.updateProduct(1L, putVm);
        
        assertEquals("Updated Product", product.getName());
    }

    // --- Tests for getProductDetail ---
    @Test
    void getProductDetail_NotFound_ThrowsException() {
        when(productRepository.findBySlugAndIsPublishedTrue("invalid")).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> productService.getProductDetail("invalid"));
        assertEquals(String.format("Product %s is not found", "invalid"), exception.getMessage());
    }

    @Test
    void getProductDetail_Success() {
        when(productRepository.findBySlugAndIsPublishedTrue("test")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        com.yas.product.viewmodel.product.ProductDetailGetVm result = productService.getProductDetail("test");
        assertThat(result).isNotNull();
        assertEquals("Test Product", result.name());
    }

    // --- Tests for deleteProduct ---
    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> productService.deleteProduct(99L));
        assertEquals(String.format("Product %s is not found", 99L), exception.getMessage());
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        productService.deleteProduct(1L);
        assertThat(product.isPublished()).isFalse();
    }

    // --- Tests for getProductsByMultiQuery ---
    @Test
    void getProductsByMultiQuery_Success() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByProductNameAndCategorySlugAndPriceBetween(
            any(), any(), any(), any(), any())).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        
        com.yas.product.viewmodel.product.ProductsGetVm result = productService.getProductsByMultiQuery(0, 10, "name", "slug", 1.0, 100.0);
        assertThat(result.productContent()).hasSize(1);
    }

    // --- Tests for getFeaturedProductsById ---
    @Test
    void getFeaturedProductsById_Success() {
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        List<com.yas.product.viewmodel.product.ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));
        assertThat(result).hasSize(1);
    }

    // --- Tests for getProductsWithFilter ---
    @Test
    void getProductsWithFilter_Success() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.getProductsWithFilter(any(), any(), any())).thenReturn(page);
        com.yas.product.viewmodel.product.ProductListGetVm result = productService.getProductsWithFilter(0, 10, "name", "brand");
        assertThat(result.productContent()).hasSize(1);
    }

    // --- Tests for exportProducts ---
    @Test
    void exportProducts_Success() {
        when(productRepository.getExportingProducts(any(), any())).thenReturn(List.of(product));
        List<com.yas.product.viewmodel.product.ProductExportingDetailVm> result = productService.exportProducts("name", "brand");
        assertThat(result).hasSize(1);
    }

    // --- Tests for getRelatedProductsBackoffice ---
    @Test
    void getRelatedProductsBackoffice_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> productService.getRelatedProductsBackoffice(1L));
        assertEquals(String.format("Product %s is not found", 1L), exception.getMessage());
    }

    @Test
    void getRelatedProductsBackoffice_Success() {
        ProductRelated related = new ProductRelated();
        related.setRelatedProduct(product);
        product.setRelatedProducts(List.of(related));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        List<ProductListVm> result = productService.getRelatedProductsBackoffice(1L);
        assertThat(result).hasSize(1);
    }

    // --- Tests for getProductEsDetailById ---
    @Test
    void getProductEsDetailById_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductEsDetailById(1L));
    }

    @Test
    void getProductEsDetailById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        com.yas.product.viewmodel.product.ProductEsDetailVm result = productService.getProductEsDetailById(1L);
        assertThat(result).isNotNull();
    }

    // --- Tests for getRelatedProductsStorefront ---
    @Test
    void getRelatedProductsStorefront_Success() {
        product.setPublished(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        ProductRelated related = new ProductRelated();
        related.setRelatedProduct(product);
        Page<ProductRelated> page = new PageImpl<>(List.of(related));
        when(productRelatedRepository.findAllByProduct(any(), any())).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        com.yas.product.viewmodel.product.ProductsGetVm result = productService.getRelatedProductsStorefront(1L, 0, 10);
        assertThat(result.productContent()).hasSize(1);
    }

    // --- Tests for getProductsForWarehouse ---
    @Test
    void getProductsForWarehouse_Success() {
        when(productRepository.findProductForWarehouse(any(), any(), any(), any())).thenReturn(List.of(product));
        List<com.yas.product.viewmodel.product.ProductInfoVm> result = productService.getProductsForWarehouse("name", "sku", List.of(1L), com.yas.product.model.enumeration.FilterExistInWhSelection.ALL);
        assertThat(result).hasSize(1);
    }

    // --- Tests for getProductSlug ---
    @Test
    void getProductSlug_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        com.yas.product.viewmodel.product.ProductSlugGetVm result = productService.getProductSlug(1L);
        assertThat(result.slug()).isEqualTo("test-product");
    }

    // --- Tests for getProductByIds ---
    @Test
    void getProductByIds_Success() {
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
        List<ProductListVm> result = productService.getProductByIds(List.of(1L));
        assertThat(result).hasSize(1);
    }

    // --- Tests for getProductByCategoryIds ---
    @Test
    void getProductByCategoryIds_Success() {
        when(productRepository.findByCategoryIdsIn(List.of(1L))).thenReturn(List.of(product));
        List<ProductListVm> result = productService.getProductByCategoryIds(List.of(1L));
        assertThat(result).hasSize(1);
    }

    // --- Tests for getProductByBrandIds ---
    @Test
    void getProductByBrandIds_Success() {
        when(productRepository.findByBrandIdsIn(List.of(1L))).thenReturn(List.of(product));
        List<ProductListVm> result = productService.getProductByBrandIds(List.of(1L));
        assertThat(result).hasSize(1);
    }

    // --- Tests for getProductCheckoutList ---
    // --- Additional Tests for complex logic ---
    @Test
    void setProductImages_NewAndDeletedImages_Success() {
        product.setProductImages(new ArrayList<>());
        ProductImage oldImage = ProductImage.builder().imageId(10L).product(product).build();
        product.getProductImages().add(oldImage);

        List<Long> newImageIds = List.of(20L); // 10L is deleted, 20L is added
        
        List<ProductImage> result = productService.setProductImages(newImageIds, product);
        
        assertThat(result).hasSize(1);
        assertEquals(20L, result.get(0).getImageId());
        verify(productImageRepository).deleteByImageIdInAndProductId(List.of(10L), 1L);
    }

    @Test
    void setProductCategories_CategoryNotFound_ThrowsException() {
        when(categoryRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(category)); // only 1 found
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ProductPostVm postVm = new ProductPostVm("test", "slug", null, new ArrayList<>(List.of(1L, 2L)), null, null, null, "sku", "gtin", 0.0, null, 10.0, 5.0, 0.0, 0.0, true, true, true, true, true, null, null, null, null, null, List.of(), null, null, null, null);

        com.yas.commonlibrary.exception.BadRequestException exception = assertThrows(
            com.yas.commonlibrary.exception.BadRequestException.class,
            () -> productService.createProduct(postVm)
        );
        assertThat(exception.getMessage()).contains("Category [2] is not found");
    }

    @Test
    void validateProductVm_SkuDuplicatedInVariations_ThrowsException() {
        ProductVariationPostVm variation1 = new ProductVariationPostVm("var1", "slug1", "dup-sku", "gtin1", 10.0, null, null, Map.of());
        ProductVariationPostVm variation2 = new ProductVariationPostVm("var2", "slug2", "dup-sku", "gtin2", 10.0, null, null, Map.of());

        ProductPostVm postVm = new ProductPostVm("test", "main-slug", null, List.of(1L), null, null, null, "main-sku", "main-gtin", 0.0, null, 10.0, 5.0, 0.0, 0.0, true, true, true, true, true, null, null, null, null, null, List.of(variation1, variation2), null, null, null, null);

        assertThrows(DuplicatedException.class, () -> productService.createProduct(postVm));
    }

    @org.junit.jupiter.api.Disabled
    @Test
    void createProduct_WithVariations_Success() {
        ProductVariationPostVm variation1 = new ProductVariationPostVm("var1", "slug1", "sku1", "gtin1", 10.0, null, null, Map.of(1L, "Value1"));
        ProductOptionValuePostVm optionValuePostVm = new ProductOptionValuePostVm(1L, "type", 1, List.of("Value1"));
        
        ProductPostVm postVm = new ProductPostVm("test", "main-slug", null, List.of(1L), null, null, null, "main-sku", "main-gtin", 0.0, null, 10.0, 5.0, 0.0, 0.0, true, true, true, true, true, null, null, null, null, null, List.of(variation1), List.of(optionValuePostVm), null, null, null);

        when(categoryRepository.findAllById(any())).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(100L);
            return p;
        });
        ProductOption productOption = new ProductOption();
        productOption.setId(1L);
        productOption.setName("Color");
        when(productOptionRepository.findAllById(any())).thenReturn(List.of(productOption));
        when(productRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductGetDetailVm result = productService.createProduct(postVm);

        assertThat(result.name()).isEqualTo("test");
        verify(productRepository, atLeastOnce()).save(any());
        verify(productRepository).saveAll(any());
    }

    @Test
    void updateProductQuantity_Success() {
        ProductQuantityPostVm quantityVm = new ProductQuantityPostVm(1L, 10L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(5L);
        product.setStockTrackingEnabled(true);

        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        productService.updateProductQuantity(List.of(quantityVm));

        assertThat(product.getStockQuantity()).isEqualTo(10L);
    }

    @Test
    void subtractStockQuantity_Success() {
        ProductQuantityPutVm quantityVm = new ProductQuantityPutVm(1L, 2L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(true);

        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(quantityVm));

        assertThat(product.getStockQuantity()).isEqualTo(8L);
    }

    @Test
    void restoreStockQuantity_Success() {
        ProductQuantityPutVm quantityVm = new ProductQuantityPutVm(1L, 2L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(true);

        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(quantityVm));

        assertThat(product.getStockQuantity()).isEqualTo(12L);
    }
}
