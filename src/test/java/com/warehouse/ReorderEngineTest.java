package com.warehouse;

import com.warehouse.model.ReorderRecommendation;
import com.warehouse.model.Product;
import com.warehouse.model.StockOutTransaction;
import com.warehouse.model.Supplier;
import com.warehouse.model.enums.ReorderPriority;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.repository.TransactionRepository;
import com.warehouse.service.ReorderRecommendationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReorderEngineTest {
    private ReorderRecommendationEngine reorderEngine;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabase.initialize();
        reorderEngine = new ReorderRecommendationEngine(new ProductRepository(), new SupplierRepository(), new TransactionRepository());
    }

    @Test
    @DisplayName("Test reorder recommendation calculation and prioritization")
    void testReorderRecommendationCalculation() throws Exception {
        List<ReorderRecommendation> recommendations = reorderEngine.generateRecommendations();
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Verify out-of-stock items have CRITICAL priority
        boolean hasCritical = recommendations.stream().anyMatch(r -> r.getPriority() == ReorderPriority.CRITICAL);
        assertTrue(hasCritical, "Recommendations should contain CRITICAL priority for out-of-stock items.");

        // Verify formula: Reorder Point = (Avg Demand * Lead Time) + Safety Stock
        ReorderRecommendation rec = recommendations.get(0);
        int expectedReorderPoint = (rec.getAverageDailyDemand() * rec.getSupplierLeadTime()) + rec.getSafetyStock();
        assertEquals(expectedReorderPoint, rec.getReorderPoint());
    }

    @Test
    @DisplayName("Reorder rules distinguish healthy, threshold, low, and zero-stock products")
    void testExplainableReorderBoundaries() throws Exception {
        TestDatabase.clear();
        Supplier supplier = new Supplier("Reliable Supply", "Contact", "supply@example.test", 2, 5.0);
        new SupplierRepository().save(supplier);
        ProductRepository products = new ProductRepository();
        TransactionRepository transactions = new TransactionRepository();

        Product healthy = products.save(new Product("Healthy", "HEALTHY", "Test", "", 1.0, 20, 5, supplier.getId()));
        Product threshold = products.save(new Product("Threshold", "THRESHOLD", "Test", "", 1.0, 17, 5, supplier.getId()));
        Product low = products.save(new Product("Low", "LOW", "Test", "", 1.0, 10, 5, supplier.getId()));
        Product zero = products.save(new Product("Zero", "ZERO", "Test", "", 1.0, 0, 2, supplier.getId()));

        LocalDateTime today = LocalDateTime.now();
        for (Product product : List.of(healthy, threshold, low)) {
            transactions.save(new StockOutTransaction(null, product.getId(), 6, today, "Demand", "SO-1", "Zone"));
        }

        List<ReorderRecommendation> recommendations = reorderEngine.generateRecommendations();
        assertFalse(recommendations.stream().anyMatch(r -> r.getProductId().equals(healthy.getId())));

        ReorderRecommendation thresholdRecommendation = find(recommendations, threshold.getId());
        assertEquals(17, thresholdRecommendation.getReorderPoint());
        assertEquals(5, thresholdRecommendation.getRecommendedQuantity());
        assertEquals(ReorderPriority.LOW, thresholdRecommendation.getPriority());

        ReorderRecommendation lowRecommendation = find(recommendations, low.getId());
        assertEquals(ReorderPriority.MEDIUM, lowRecommendation.getPriority());
        assertTrue(lowRecommendation.getRecommendationReason().contains("6 units were dispatched"));

        ReorderRecommendation zeroRecommendation = find(recommendations, zero.getId());
        assertEquals(ReorderPriority.CRITICAL, zeroRecommendation.getPriority());
        assertEquals(2, zeroRecommendation.getReorderPoint());
        assertEquals(zero.getId(), recommendations.getFirst().getProductId(),
                "Critical recommendations must be shown before lower priorities.");
    }

    private ReorderRecommendation find(List<ReorderRecommendation> recommendations, Long productId) {
        return recommendations.stream().filter(r -> r.getProductId().equals(productId)).findFirst().orElseThrow();
    }
}
