package com.yas.recommendation.kafka.consumer;

import static org.mockito.Mockito.*;

import com.yas.commonlibrary.kafka.cdc.message.ProductCdcMessage;
import com.yas.commonlibrary.kafka.cdc.message.ProductMsgKey;
import com.yas.recommendation.vector.product.service.ProductVectorSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class ProductSyncDataConsumerTest {

    @Mock
    private ProductVectorSyncService productVectorSyncService;

    private ProductSyncDataConsumer productSyncDataConsumer;

    @BeforeEach
    void setUp() {
        productSyncDataConsumer = new ProductSyncDataConsumer(productVectorSyncService);
    }

    @Test
    void processMessage_shouldCallServiceSync() {
        ProductMsgKey key = new ProductMsgKey(1L);
        ProductCdcMessage message = new ProductCdcMessage();
        MessageHeaders headers = new MessageHeaders(new HashMap<>());

        productSyncDataConsumer.processMessage(key, message, headers);

        verify(productVectorSyncService).sync(key, message);
    }
}
