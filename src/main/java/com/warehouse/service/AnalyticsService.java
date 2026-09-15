package com.warehouse.service;

import com.warehouse.exception.DatabaseException;
import com.warehouse.model.Product;
import com.warehouse.model.Supplier;
import com.warehouse.model.StockTransaction;
import com.warehouse.model.enums.TransactionType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service calculating warehouse inventory analytics and statistics.
 * Demonstrates CSE2006 Unit 4: Java Collections & Streams (groupingBy, collectors, sorting, filtering).
 */
public class AnalyticsService {
    private final ProductService productService;
    private final SupplierService supplierService;
    private final TransactionService transactionService;

    public AnalyticsService(ProductService productService, SupplierService supplierService,
                            TransactionService transactionService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.transactionService = transactionService;
    }

    public int getTotalProductCount() throws DatabaseException {
        return productService.getAllProducts().size();
    }

    public int getTotalInventoryQuantity() throws DatabaseException {
        return productService.getAllProducts().stream()
                .mapToInt(Product::getQuantity)
                .sum();
    }

    public double getTotalInventoryValuation() throws DatabaseException {
        return productService.getAllProducts().stream()
                .mapToDouble(Product::getTotalValue)
                .sum();
    }

    public long getLowStockCount() throws DatabaseException {
        return productService.getAllProducts().stream()
                .filter(Product::isLowStock)
                .count();
    }

    public long getOutOfStockCount() throws DatabaseException {
        return productService.getAllProducts().stream()
                .filter(Product::isOutOfStock)
                .count();
    }

    /** Out-of-stock products are the system's critical inventory condition. */
    public long getCriticalStockCount() throws DatabaseException {
        return getOutOfStockCount();
    }

    public int getTotalStockInQuantity() throws DatabaseException {
        return getTransactionQuantityByType(TransactionType.STOCK_IN);
    }

    public int getTotalStockOutQuantity() throws DatabaseException {
        return getTransactionQuantityByType(TransactionType.STOCK_OUT);
    }

    public Map<TransactionType, Long> getTransactionCountByType() throws DatabaseException {
        return transactionService.getAllTransactions().stream()
                .collect(Collectors.groupingBy(StockTransaction::getType,
                        () -> new EnumMap<>(TransactionType.class), Collectors.counting()));
    }

    /** Product IDs are retained as map keys to avoid merging distinct products with the same display name. */
    public Map<Long, Long> getMovementCountByProduct() throws DatabaseException {
        return transactionService.getAllTransactions().stream()
                .collect(Collectors.groupingBy(StockTransaction::getProductId, Collectors.counting()));
    }

    public List<Long> getMostFrequentlyMovedProductIds(int limit) throws DatabaseException {
        if (limit <= 0) {
            return List.of();
        }
        return getMovementCountByProduct().entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    public Map<String, Long> getProductCountByCategory() throws DatabaseException {
        return productService.getAllProducts().stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
    }

    public Map<String, Double> getInventoryValuationByCategory() throws DatabaseException {
        return productService.getAllProducts().stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.summingDouble(Product::getTotalValue)
                ));
    }

    public List<Product> getTopHighestValueProducts(int limit) throws DatabaseException {
        return productService.getAllProducts().stream()
                .sorted(Comparator.comparingDouble(Product::getTotalValue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getProductsPerSupplier() throws DatabaseException {
        List<Product> products = productService.getAllProducts();
        Map<Long, String> supplierNameMap = supplierService.getAllSuppliers().stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getName));

        return products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSupplierId() != null && supplierNameMap.containsKey(p.getSupplierId())
                                ? supplierNameMap.get(p.getSupplierId())
                                : "Unassigned Supplier",
                        Collectors.counting()
                ));
    }

    public String generateAnalyticsSummaryReport() throws DatabaseException {
        Map<TransactionType, Long> transactionCounts = getTransactionCountByType();
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("           WAREHOUSE ANALYTICS SUMMARY            \n");
        sb.append("==================================================\n");
        sb.append(String.format("Total Catalog SKUs          : %d\n", getTotalProductCount()));
        sb.append(String.format("Total Units in Stock        : %d\n", getTotalInventoryQuantity()));
        sb.append(String.format("Total Inventory Value       : $%.2f\n", getTotalInventoryValuation()));
        sb.append(String.format("Low Stock Warning Items     : %d\n", getLowStockCount()));
        sb.append(String.format("Out of Stock Items          : %d\n", getOutOfStockCount()));
        sb.append(String.format("Critical Stock Items        : %d\n", getCriticalStockCount()));
        sb.append(String.format("Recorded Stock-in Units     : %d\n", getTotalStockInQuantity()));
        sb.append(String.format("Recorded Stock-out Units    : %d\n", getTotalStockOutQuantity()));
        sb.append("--------------------------------------------------\n");
        sb.append("TRANSACTION COUNTS:\n");
        for (TransactionType type : TransactionType.values()) {
            sb.append(String.format("  %-22s : %d\n", type.getDescription(), transactionCounts.getOrDefault(type, 0L)));
        }
        sb.append("--------------------------------------------------\n");
        sb.append("CATEGORY BREAKDOWN (Product Count):\n");
        getProductCountByCategory().forEach((cat, count) ->
                sb.append(String.format("  • %-22s : %d items\n", cat, count)));
        sb.append("--------------------------------------------------\n");
        sb.append("CATEGORY BREAKDOWN (Inventory Valuation):\n");
        getInventoryValuationByCategory().forEach((cat, val) ->
                sb.append(String.format("  • %-22s : $%.2f\n", cat, val)));
        sb.append("--------------------------------------------------\n");
        sb.append("TOP HIGHEST VALUE PRODUCTS:\n");
        int rank = 1;
        for (Product p : getTopHighestValueProducts(5)) {
            sb.append(String.format("  %d. %-22s | Qty: %3d | Value: $%.2f\n", rank++, p.getName(), p.getQuantity(), p.getTotalValue()));
        }
        sb.append("==================================================\n");
        return sb.toString();
    }

    private int getTransactionQuantityByType(TransactionType type) throws DatabaseException {
        return transactionService.getAllTransactions().stream()
                .filter(transaction -> transaction.getType() == type)
                .mapToInt(StockTransaction::getQuantity)
                .sum();
    }
}
