package com.warehouse;

import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.repository.TransactionRepository;
import com.warehouse.service.AnalyticsService;
import com.warehouse.service.ProductService;
import com.warehouse.service.SupplierService;
import com.warehouse.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import com.warehouse.model.enums.TransactionType;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabase.initialize();
        ProductRepository prodRepo = new ProductRepository();
        SupplierRepository suppRepo = new SupplierRepository();
        ProductService prodService = new ProductService(prodRepo, suppRepo);
        SupplierService suppService = new SupplierService(suppRepo);
        analyticsService = new AnalyticsService(prodService, suppService, new TransactionService(new TransactionRepository()));
    }

    @Test
    @DisplayName("Test overall inventory analytics calculations")
    void testAnalyticsCalculations() throws Exception {
        assertTrue(analyticsService.getTotalProductCount() > 0);
        assertTrue(analyticsService.getTotalInventoryQuantity() > 0);
        assertTrue(analyticsService.getTotalInventoryValuation() > 0.0);

        Map<String, Long> categoryCountMap = analyticsService.getProductCountByCategory();
        assertNotNull(categoryCountMap);
        assertFalse(categoryCountMap.isEmpty());

        Map<String, Double> categoryValuationMap = analyticsService.getInventoryValuationByCategory();
        assertNotNull(categoryValuationMap);
        assertFalse(categoryValuationMap.isEmpty());
        assertTrue(analyticsService.getTotalStockInQuantity() > 0);
        assertTrue(analyticsService.getTotalStockOutQuantity() > 0);
        assertTrue(analyticsService.getTransactionCountByType().get(TransactionType.STOCK_OUT) > 0);
        assertFalse(analyticsService.getMostFrequentlyMovedProductIds(3).isEmpty());
    }

    @Test
    @DisplayName("Analytics return zero values and empty collections for an empty warehouse")
    void testEmptyWarehouseAnalytics() throws Exception {
        TestDatabase.clear();
        assertEquals(0, analyticsService.getTotalProductCount());
        assertEquals(0, analyticsService.getTotalInventoryQuantity());
        assertEquals(0.0, analyticsService.getTotalInventoryValuation());
        assertEquals(0, analyticsService.getTotalStockInQuantity());
        assertEquals(0, analyticsService.getTotalStockOutQuantity());
        assertTrue(analyticsService.getTransactionCountByType().isEmpty());
        assertTrue(analyticsService.getMostFrequentlyMovedProductIds(5).isEmpty());
    }
}
