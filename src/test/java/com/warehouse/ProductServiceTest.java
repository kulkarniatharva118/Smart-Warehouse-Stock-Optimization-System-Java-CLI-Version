package com.warehouse;

import com.warehouse.exception.DuplicateSkuException;
import com.warehouse.exception.InvalidInputException;
import com.warehouse.exception.ProductNotFoundException;
import com.warehouse.exception.SupplierNotFoundException;
import com.warehouse.model.Product;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {
    private ProductService productService;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabase.initialize();
        productService = new ProductService(new ProductRepository(), new SupplierRepository());
    }

    @Test
    @DisplayName("Test adding a new valid product")
    void testAddProductSuccess() throws Exception {
        String uniqueSku = "TEST-SKU-" + System.currentTimeMillis();
        Product product = new Product("Test Gaming Mouse", uniqueSku, "Electronics", "High DPI", 49.99, 20, 5, 1L);

        Product saved = productService.addProduct(product);
        assertNotNull(saved.getId());
        assertEquals("Test Gaming Mouse", saved.getName());
    }

    @Test
    @DisplayName("Test adding product with duplicate SKU throws DuplicateSkuException")
    void testAddProductDuplicateSkuThrowsException() {
        Product duplicate = new Product("Duplicate Item", "SKU-1001", "Electronics", "Test", 10.0, 5, 2, 1L);

        assertThrows(DuplicateSkuException.class, () -> productService.addProduct(duplicate));
    }

    @Test
    @DisplayName("Test adding product with negative price throws InvalidInputException")
    void testAddProductNegativePriceThrowsException() {
        Product invalidPrice = new Product("Negative Price", "SKU-NEG-1", "Electronics", "Test", -15.0, 5, 2, 1L);

        assertThrows(InvalidInputException.class, () -> productService.addProduct(invalidPrice));
    }

    @Test
    @DisplayName("Test querying non-existent product ID throws ProductNotFoundException")
    void testProductNotFoundThrowsException() {
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999999L));
    }

    @Test
    @DisplayName("Test a product cannot reference a missing supplier")
    void testMissingSupplierThrowsException() {
        Product product = new Product("Orphaned Item", "ORPHAN-SKU", "Testing", "Test", 10.0, 1, 0, 999999L);
        assertThrows(SupplierNotFoundException.class, () -> productService.addProduct(product));
    }

    @Test
    @DisplayName("Test searching products by name")
    void testSearchProductByName() throws Exception {
        List<Product> matches = productService.searchProductsByName("Mouse");
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(p -> p.getName().contains("Mouse")));
    }
}
