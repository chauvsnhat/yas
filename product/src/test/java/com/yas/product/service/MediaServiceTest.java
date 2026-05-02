package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.product.viewmodel.NoFileMediaVm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private MediaService mediaService;

    @Test
    void getMedia_NullId_ReturnsDefault() {
        NoFileMediaVm result = mediaService.getMedia(null);
        assertNull(result.id());
        assertEquals("", result.url());
    }
}
