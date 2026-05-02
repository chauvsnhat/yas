package com.yas.recommendation.vector.common.document;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DefaultIdGeneratorTest {

    @Test
    void generateId_shouldReturnConsistentUuid() {
        String prefix = "PRODUCT";
        Long entityId = 123L;
        DefaultIdGenerator generator = new DefaultIdGenerator(prefix, entityId);

        String result1 = generator.generateId();
        String result2 = generator.generateId();

        assertNotNull(result1);
        assertEquals(result1, result2, "Generated ID must be consistent for same prefix and entityId");
        
        // Test different entity ID
        DefaultIdGenerator generator2 = new DefaultIdGenerator(prefix, 124L);
        String result3 = generator2.generateId();
        assertNotEquals(result1, result3, "Generated IDs must be different for different entityIds");
    }
}
