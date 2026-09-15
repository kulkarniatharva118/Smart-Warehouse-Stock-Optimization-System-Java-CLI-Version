package com.warehouse;

import com.warehouse.exception.InsufficientStockException;
import com.warehouse.model.Product;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.repository.TransactionRepository;
import com.warehouse.service.InventoryService;
import com.warehouse.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyTest {
    private InventoryService inventoryService;
    private ProductService productService;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabase.initialize();
        ProductRepository prodRepo = new ProductRepository();
        inventoryService = new InventoryService(prodRepo, new TransactionRepository());
        productService = new ProductService(prodRepo, new SupplierRepository());
    }

    @Test
    @DisplayName("Test multi-threaded concurrent stock-out operations are thread-safe")
    void testConcurrentStockOutOperationsThreadSafety() throws Exception {
        // Create a unique product dedicated to concurrency test with exact stock = 10
        String sku = "CONCURRENCY-SKU-" + System.currentTimeMillis();
        Product product = productService.addProduct(new Product("Concurrency Test Item", sku, "Testing", "Race condition check", 10.0, 10, 2, 1L));

        int threadCount = 12; // 12 concurrent threads attempting to deduct 1 item each (Stock = 10)
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientStockCount = new AtomicInteger(0);
        AtomicReference<Exception> unexpectedFailure = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for simultaneous start signal
                    inventoryService.stockOut(product.getId(), 1, "Concurrent Order #" + threadId, "SO-CONC-" + threadId, "Zone A");
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    insufficientStockCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedFailure.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release all threads simultaneously
        doneLatch.await(); // Wait for all threads to finish
        executor.shutdown();

        assertNull(unexpectedFailure.get(), "Concurrent operations must not fail unexpectedly.");

        Product finalProductState = productService.getProductById(product.getId());

        // Assertions: Exactly 10 stock-out operations succeeded, 2 failed due to InsufficientStockException, final stock is 0
        assertEquals(10, successCount.get(), "Exactly 10 stock-out operations should succeed.");
        assertEquals(2, insufficientStockCount.get(), "Exactly 2 stock-out operations should fail with InsufficientStockException.");
        assertEquals(0, finalProductState.getQuantity(), "Final product stock should be 0 and never negative.");
        assertEquals(10, new TransactionRepository().findByProductId(product.getId()).size(),
                "Each successful stock-out must have exactly one committed transaction record.");
    }
}
