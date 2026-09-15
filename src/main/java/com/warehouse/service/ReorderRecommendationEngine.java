package com.warehouse.service;

import com.warehouse.exception.DatabaseException;
import com.warehouse.model.Product;
import com.warehouse.model.ReorderRecommendation;
import com.warehouse.model.StockTransaction;
import com.warehouse.model.Supplier;
import com.warehouse.model.enums.ReorderPriority;
import com.warehouse.model.enums.TransactionType;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart Reorder Recommendation Engine.
 * Calculates optimal stock reorder points and suggested reorder quantities.
 * Formula: Reorder Point = (Average Daily Demand × Supplier Lead Time) + Safety Stock
 * Demonstrates CSE2006 Unit 4: Java Collections & Streams filtering, mapping, sorting.
 */
public class ReorderRecommendationEngine {
    /** Lead time used only when a product has no assigned supplier. */
    private static final int DEFAULT_LEAD_TIME_DAYS = 7;

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final TransactionRepository transactionRepository;

    public ReorderRecommendationEngine(ProductRepository productRepository, SupplierRepository supplierRepository,
                                       TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Evaluates all products in the warehouse and generates reorder recommendations for items below reorder point.
     *
     * @return List of ReorderRecommendation objects
     */
    public List<ReorderRecommendation> generateRecommendations() throws DatabaseException {
        List<Product> products = productRepository.findAll();
        Map<Long, Supplier> supplierMap = supplierRepository.findAll().stream()
                .collect(Collectors.toMap(Supplier::getId, s -> s));
        Map<Long, List<StockTransaction>> stockOutByProduct = transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getType() == TransactionType.STOCK_OUT)
                .collect(Collectors.groupingBy(StockTransaction::getProductId));

        List<ReorderRecommendation> recommendations = new ArrayList<>();

        for (Product product : products) {
            int leadTimeDays = DEFAULT_LEAD_TIME_DAYS;
            if (product.getSupplierId() != null && supplierMap.containsKey(product.getSupplierId())) {
                leadTimeDays = supplierMap.get(product.getSupplierId()).getLeadTimeDays();
            }

            List<StockTransaction> stockOutTransactions = stockOutByProduct.getOrDefault(product.getId(), List.of());
            int avgDailyDemand = calculateAverageDailyDemand(stockOutTransactions);
            int safetyStock = product.getMinimumStockLevel();

            // Reorder point = demand expected while waiting for the supplier + configured safety stock.
            int reorderPoint = (avgDailyDemand * leadTimeDays) + safetyStock;

            if (product.getQuantity() <= reorderPoint) {
                int recommendedQty = (reorderPoint + safetyStock) - product.getQuantity();

                ReorderPriority priority;
                if (product.getQuantity() <= 0) {
                    priority = ReorderPriority.CRITICAL;
                } else if (product.getQuantity() <= safetyStock) {
                    priority = ReorderPriority.HIGH;
                } else if (product.getQuantity() < reorderPoint) {
                    priority = ReorderPriority.MEDIUM;
                } else {
                    priority = ReorderPriority.LOW;
                }

                recommendations.add(new ReorderRecommendation(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getQuantity(),
                        avgDailyDemand,
                        leadTimeDays,
                        safetyStock,
                        reorderPoint,
                        recommendedQty,
                        priority,
                        buildReason(stockOutTransactions, avgDailyDemand, leadTimeDays, safetyStock)
                ));
            }
        }

        // Sort by priority (CRITICAL -> HIGH -> MEDIUM -> LOW) using Streams
        return recommendations.stream()
                .sorted(Comparator.comparing(ReorderRecommendation::getPriority).reversed()
                        .thenComparing(ReorderRecommendation::getProductId))
                .collect(Collectors.toList());
    }

    /**
     * Calculates observed daily demand from stock-out history. For example, 12 units dispatched
     * between 1 and 3 March is 4 units/day (12 / 3 inclusive calendar days). No stock-out history
     * produces zero demand; the configured safety stock still remains the reorder threshold.
     */
    private int calculateAverageDailyDemand(List<StockTransaction> stockOutTransactions) {
        if (stockOutTransactions.isEmpty()) {
            return 0;
        }
        int totalStockOut = stockOutTransactions.stream().mapToInt(StockTransaction::getQuantity).sum();
        LocalDate firstDate = stockOutTransactions.stream().map(tx -> tx.getTimestamp().toLocalDate()).min(LocalDate::compareTo).orElseThrow();
        LocalDate lastDate = stockOutTransactions.stream().map(tx -> tx.getTimestamp().toLocalDate()).max(LocalDate::compareTo).orElseThrow();
        long observedDays = ChronoUnit.DAYS.between(firstDate, lastDate) + 1;
        return (int) Math.ceil((double) totalStockOut / observedDays);
    }

    private String buildReason(List<StockTransaction> stockOutTransactions, int dailyDemand,
                               int leadTimeDays, int safetyStock) {
        if (stockOutTransactions.isEmpty()) {
            return "Current stock is at or below the configured safety-stock threshold of " + safetyStock
                    + "; no stock-out history is available.";
        }
        int dispatchedUnits = stockOutTransactions.stream().mapToInt(StockTransaction::getQuantity).sum();
        return dispatchedUnits + " units were dispatched in " + stockOutTransactions.size()
                + " recorded stock-out transaction(s), averaging " + dailyDemand + " units/day. "
                + "The reorder point covers " + leadTimeDays + " supplier lead-time day(s) plus safety stock.";
    }
}
