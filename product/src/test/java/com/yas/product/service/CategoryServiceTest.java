package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Category;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.category.CategoryGetDetailVm;
import com.yas.product.viewmodel.category.CategoryGetVm;
import com.yas.product.viewmodel.category.CategoryListGetVm;
import com.yas.product.viewmodel.category.CategoryPostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private MediaService mediaService;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("name");
        category.setSlug("slug");
        category.setImageId(1L);
        category.setDisplayOrder((short) 1);

        noFileMediaVm = new NoFileMediaVm(1L, "caption", "fileName", "mediaType", "url");
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        
        CategoryGetDetailVm result = categoryService.getCategoryById(1L);
        
        assertNotNull(result);
        assertEquals("name", result.name());
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void create_DuplicateName_ThrowsException() {
        CategoryPostVm postVm = new CategoryPostVm("name", "slug", "desc", null, "keywords", "metaDesc", (short) 1, true, 1L);
        when(categoryRepository.findExistedName("name", null)).thenReturn(new Category());
        
        assertThrows(DuplicatedException.class, () -> categoryService.create(postVm));
    }

    @Test
    void create_WithParent_Success() {
        CategoryPostVm postVm = new CategoryPostVm("child", "slug", "desc", 1L, "keywords", "metaDesc", (short) 1, true, 1L);
        when(categoryRepository.findExistedName("child", null)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        Category result = categoryService.create(postVm);
        
        assertNotNull(result);
        assertEquals("child", result.getName());
        assertEquals(category, result.getParent());
    }

    @Test
    void create_ParentNotFound_ThrowsException() {
        CategoryPostVm postVm = new CategoryPostVm("child", "slug", "desc", 2L, "keywords", "metaDesc", (short) 1, true, 1L);
        when(categoryRepository.findExistedName("child", null)).thenReturn(null);
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());
        
        assertThrows(BadRequestException.class, () -> categoryService.create(postVm));
    }

    @Test
    void update_CategoryNotFound_ThrowsException() {
        CategoryPostVm postVm = new CategoryPostVm("name", "slug", "desc", null, "keywords", "metaDesc", (short) 1, true, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> categoryService.update(postVm, 1L));
    }

    @Test
    void update_ItselfAsParent_ThrowsException() {
        CategoryPostVm postVm = new CategoryPostVm("name", "slug", "desc", 1L, "keywords", "metaDesc", (short) 1, true, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        
        assertThrows(BadRequestException.class, () -> categoryService.update(postVm, 1L));
    }

    @Test
    void getCategories_Success() {
        when(categoryRepository.findByNameContainingIgnoreCase("name")).thenReturn(List.of(category));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);
        
        List<CategoryGetVm> result = categoryService.getCategories("name");
        
        assertEquals(1, result.size());
        assertEquals("url", result.get(0).categoryImage().url());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPageableCategories_Success() {
        Page<Category> categoryPage = mock(Page.class);
        when(categoryPage.getContent()).thenReturn(List.of(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        
        CategoryListGetVm result = categoryService.getPageableCategories(0, 10);
        
        assertNotNull(result);
        assertEquals(1, result.categoryContent().size());
    }

    @Test
    void getCategoryByIds_Success() {
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        List<CategoryGetVm> result = categoryService.getCategoryByIds(List.of(1L));
        assertEquals(1, result.size());
    }

    @Test
    void getTopNthCategories_Success() {
        when(categoryRepository.findCategoriesOrderedByProductCount(any())).thenReturn(List.of("Category A"));
        List<String> result = categoryService.getTopNthCategories(5);
        assertEquals(1, result.size());
        assertEquals("Category A", result.get(0));
    }
}