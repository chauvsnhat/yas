package com.yas.recommendation.vector.product.service;

import com.yas.commonlibrary.kafka.cdc.message.Operation;
import com.yas.commonlibrary.kafka.cdc.message.Product;
import com.yas.commonlibrary.kafka.cdc.message.ProductCdcMessage;
import com.yas.commonlibrary.kafka.cdc.message.ProductMsgKey;
import com.yas.recommendation.vector.product.store.ProductVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for synchronizing product vector data.
 * Provides methods to create, update, and delete product vectors
 * in response to changes in product data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVectorSyncService {
    private final ProductVectorRepository productVectorRepository;

    public void sync(ProductMsgKey key, ProductCdcMessage message) {
        Operation op = message.getOp();
        log.info("Processing CDC message for product {} with operation {}", key.getId(), op);
        
        switch (op) {
            case CREATE, READ -> createProductVector(message.getAfter());
            case UPDATE -> updateProductVector(message.getAfter());
            case DELETE -> deleteProductVector(key.getId());
            default -> log.warn("Unsupported operation: {}", op);
        }
    }

    /**
     * Creates a product vector if the product is published.
     *
     * @param product {@link Product} the product to be synchronized.
     */
    public void createProductVector(Product product) {
        if (product != null && product.isPublished()) {
            log.info("Adding product vector for product {}", product.getId());
            productVectorRepository.add(product.getId());
        }
    }

    /**
     * Updates a product vector if the product is published; deletes it otherwise.
     *
     * @param product {@link Product} the product to be synchronized.
     */
    public void updateProductVector(Product product) {
        if (product != null && product.isPublished()) {
            log.info("Updating product vector for product {}", product.getId());
            productVectorRepository.update(product.getId());
        } else if (product != null) {
            log.info("Deleting product vector for product {} (unpublished)", product.getId());
            productVectorRepository.delete(product.getId());
        }
    }

    /**
     * Deletes the product vector for the specified product.
     *
     * @param productId The unique identifier of the product whose vector is to be deleted.
     */
    public void deleteProductVector(Long productId) {
        log.info("Deleting product vector for product {}", productId);
        productVectorRepository.delete(productId);
    }

}
