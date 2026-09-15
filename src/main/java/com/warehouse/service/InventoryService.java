package com.warehouse.service;

import com.warehouse.exception.*;
import com.warehouse.database.DatabaseManager;
import com.warehouse.model.*;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.TransactionRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Core inventory service performing stock mutations and inventory queries.
 * Demonstrates CSE2006 Unit 3: Thread Synchronization & Custom Exception Handling.
 *
 * WHY SYNCHRONIZATION IS REQUIRED:
 * In a multi-threaded environment (e.g. concurrent warehouse operations or background monitoring),
 * two concurrent stock-out threads attempting to deduct inventory from the same product can encounter
 * a Check-Then-Act race condition:
 * Thread A reads stock = 10 (valid for deduction of 10).
 * Thread B concurrently reads stock = 10 (valid for deduction of 10).
 * Both threads can otherwise write stale values, losing an update or allowing an invalid dispatch.
 *
 * To prevent this, per-product fine-grained synchronization locks are enforced during all stock modifications.
 */
public class InventoryService {
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;

    // Per-product lock map to ensure thread-safety per item without blocking unrelated products
    private final Map<Long, Object> productLocks = new ConcurrentHashMap<>();

    public InventoryService(ProductRepository productRepository, TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    private Object getLock(Long productId) {
        return productLocks.computeIfAbsent(productId, k -> new Object());
    }

    /**
     * Synchronized Stock In (Restock) operation.
     */
    public Product stockIn(Long productId, int quantity, String reason, String purchaseOrderNumber) throws WarehouseException {
        if (quantity <= 0) {
            throw new InvalidStockOperationException("Stock-in quantity must be greater than zero.");
        }

        synchronized (getLock(productId)) {
            return mutate(productId, product -> {
                int newQty;
                try {
                    newQty = Math.addExact(product.getQuantity(), quantity);
                } catch (ArithmeticException e) {
                    throw new InvalidStockOperationException("Stock-in would exceed the maximum supported quantity.");
                }
                return new Mutation(newQty, new StockInTransaction(productId, quantity, reason, purchaseOrderNumber));
            });
        }
    }

    /**
     * Synchronized Stock Out (Dispatch) operation.
     * Prevents negative stock via thread synchronization and stock check.
     */
    public Product stockOut(Long productId, int quantity, String reason, String salesOrderNumber, String destination) throws WarehouseException {
        if (quantity <= 0) {
            throw new InvalidStockOperationException("Stock-out quantity must be greater than zero.");
        }

        synchronized (getLock(productId)) {
            return mutate(productId, product -> {
                if (product.getQuantity() < quantity) {
                    throw new InsufficientStockException(String.format(
                            "Insufficient stock for '%s' (ID #%d). Requested: %d, Available: %d",
                            product.getName(), productId, quantity, product.getQuantity()));
                }
                return new Mutation(product.getQuantity() - quantity,
                        new StockOutTransaction(productId, quantity, reason, salesOrderNumber, destination));
            });
        }
    }

    /**
     * Synchronized Stock Return operation.
     */
    public Product returnStock(Long productId, int quantity, String reason, String returnReason, String returnSource) throws WarehouseException {
        if (quantity <= 0) {
            throw new InvalidStockOperationException("Return stock quantity must be greater than zero.");
        }

        synchronized (getLock(productId)) {
            return mutate(productId, product -> {
                int newQty;
                try {
                    newQty = Math.addExact(product.getQuantity(), quantity);
                } catch (ArithmeticException e) {
                    throw new InvalidStockOperationException("Return would exceed the maximum supported quantity.");
                }
                return new Mutation(newQty, new ReturnTransaction(productId, quantity, reason, returnReason, returnSource));
            });
        }
    }

    /**
     * Synchronized Stock Adjustment operation.
     */
    public Product adjustStock(Long productId, int newQuantity, String reason, String adjustedBy, String discrepancyReason) throws WarehouseException {
        if (newQuantity < 0) {
            throw new InvalidStockOperationException("Adjusted stock quantity cannot be negative.");
        }

        synchronized (getLock(productId)) {
            return mutate(productId, product -> {
                int diff;
                try {
                    diff = Math.subtractExact(newQuantity, product.getQuantity());
                } catch (ArithmeticException e) {
                    throw new InvalidStockOperationException("Adjustment difference is outside the supported range.");
                }
                return new Mutation(newQuantity, new AdjustmentTransaction(productId, diff, reason, adjustedBy, discrepancyReason));
            });
        }
    }

    public List<Product> getLowStockProducts() throws DatabaseException {
        return productRepository.findAll().stream()
                .filter(Product::isLowStock)
                .collect(Collectors.toList());
    }

    public List<Product> getOutOfStockProducts() throws DatabaseException {
        return productRepository.findAll().stream()
                .filter(Product::isOutOfStock)
                .collect(Collectors.toList());
    }

    private Product mutate(Long productId, MutationFactory mutationFactory) throws WarehouseException {
        if (productId == null || productId <= 0) {
            throw new InvalidStockOperationException("Product ID must be a positive number.");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Product product = productRepository.findByIdForUpdate(conn, productId)
                        .orElseThrow(() -> new ProductNotFoundException("Product ID #" + productId + " not found."));
                Mutation mutation = mutationFactory.create(product);
                productRepository.updateQuantity(conn, productId, mutation.newQuantity());
                transactionRepository.save(conn, mutation.transaction());
                conn.commit();
                product.setQuantity(mutation.newQuantity());
                return product;
            } catch (WarehouseException e) {
                rollback(conn, e);
                throw e;
            } catch (SQLException e) {
                rollback(conn, e);
                throw new DatabaseException("Inventory operation failed and was rolled back: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to start inventory operation: " + e.getMessage(), e);
        }
    }

    private void rollback(Connection conn, Exception original) throws DatabaseException {
        try {
            conn.rollback();
        } catch (SQLException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
            throw new DatabaseException("Inventory operation failed and rollback also failed.", original);
        }
    }

    @FunctionalInterface
    private interface MutationFactory {
        Mutation create(Product product) throws WarehouseException;
    }

    private record Mutation(int newQuantity, StockTransaction transaction) { }
}
