package com.warehouse;

import com.warehouse.exception.InsufficientStockException;
import com.warehouse.exception.InvalidStockOperationException;
import com.warehouse.exception.DatabaseException;
import com.warehouse.model.Product;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.repository.TransactionRepository;
import com.warehouse.service.InventoryService;
import com.warehouse.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {
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
    @DisplayName("Test stock-in operation increases quantity")
    void testStockInSuccess() throws Exception {
        Product initial = productService.getProductBySku("SKU-1001");
        int originalQty = initial.getQuantity();

        Product updated = inventoryService.stockIn(initial.getId(), 10, "Unit Test Restock", "PO-TEST-101");
        assertEquals(originalQty + 10, updated.getQuantity());
    }

    @Test
    @DisplayName("Test stock-out operation decreases quantity")
    void testStockOutSuccess() throws Exception {
        Product initial = productService.getProductBySku("SKU-1001");
        int originalQty = initial.getQuantity();

        Product updated = inventoryService.stockOut(initial.getId(), 5, "Unit Test Dispatch", "SO-TEST-201", "Zone A");
        assertEquals(originalQty - 5, updated.getQuantity());
    }

    @Test
    @DisplayName("Test stock-out exceeding available inventory throws InsufficientStockException")
    void testStockOutInsufficientStockThrowsException() throws Exception {
        Product product = productService.getProductBySku("SKU-1001");
        int excessiveQty = product.getQuantity() + 500;

        assertThrows(InsufficientStockException.class, () ->
                inventoryService.stockOut(product.getId(), excessiveQty, "Excessive Dispatch", "SO-FAIL", "Zone B"));
    }

    @Test
    @DisplayName("Test negative stock-in quantity throws InvalidStockOperationException")
    void testInvalidStockInQuantityThrowsException() throws Exception {
        Product product = productService.getProductBySku("SKU-1001");
        assertThrows(InvalidStockOperationException.class, () ->
                inventoryService.stockIn(product.getId(), -10, "Invalid Negative Qty", "PO-FAIL"));
    }

    @Test
    @DisplayName("Test return stock operation increases inventory")
    void testReturnStockSuccess() throws Exception {
        Product initial = productService.getProductBySku("SKU-1004");
        int originalQty = initial.getQuantity();

        Product updated = inventoryService.returnStock(initial.getId(), 3, "Customer Return", "Unopened", "CUSTOMER");
        assertEquals(originalQty + 3, updated.getQuantity());
    }

    @Test
    @DisplayName("Test low stock and out of stock products query")
    void testLowStockAndOutOfStockDetection() throws Exception {
        List<Product> lowStock = inventoryService.getLowStockProducts();
        List<Product> outOfStock = inventoryService.getOutOfStockProducts();

        assertNotNull(lowStock);
        assertNotNull(outOfStock);
        assertTrue(outOfStock.stream().allMatch(p -> p.getQuantity() <= 0));
        assertTrue(lowStock.stream().allMatch(p -> p.getQuantity() <= p.getMinimumStockLevel() && p.getQuantity() > 0));
    }

    @Test
    @DisplayName("Test a failed transaction insert rolls back its inventory update")
    void testStockMutationRollsBackWhenTransactionRecordingFails() throws Exception {
        ProductRepository productRepository = new ProductRepository();
        InventoryService failingInventoryService = new InventoryService(productRepository, new TransactionRepository() {
            @Override
            public com.warehouse.model.StockTransaction save(Connection connection,
                                                              com.warehouse.model.StockTransaction transaction) throws DatabaseException {
                throw new DatabaseException("Simulated transaction persistence failure");
            }
        });
        Product product = productService.getProductBySku("SKU-1001");
        int originalQuantity = product.getQuantity();

        assertThrows(DatabaseException.class, () -> failingInventoryService.stockIn(
                product.getId(), 1, "Rollback test", "PO-ROLLBACK"));

        assertEquals(originalQuantity, productService.getProductById(product.getId()).getQuantity());
    }
}
