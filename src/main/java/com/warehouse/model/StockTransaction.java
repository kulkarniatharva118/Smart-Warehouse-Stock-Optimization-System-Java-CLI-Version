package com.warehouse.model;

import com.warehouse.model.enums.TransactionType;

import java.time.LocalDateTime;

/**
 * Abstract base class for stock transactions in the warehouse system.
 * Demonstrates CSE2006 Unit 2: Abstract Classes, Inheritance, Encapsulation, and Polymorphism.
 */
public abstract class StockTransaction {
    private Long id;
    private Long productId;
    private int quantity;
    private LocalDateTime timestamp;
    private String reason;
    private TransactionType type;

    public StockTransaction() {
        this.timestamp = LocalDateTime.now();
    }

    public StockTransaction(Long id, Long productId, int quantity, LocalDateTime timestamp, String reason, TransactionType type) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.reason = reason;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * Abstract method demonstrating polymorphism.
     * Each concrete transaction subclass provides specific transaction details.
     *
     * @return Formatted string representation of transaction details.
     */
    public abstract String getTransactionDetails();

    @Override
    public String toString() {
        return String.format("Transaction[ID=%d, Type=%s, ProductID=%d, Qty=%d, Time=%s, Reason='%s']",
                id, type, productId, quantity, timestamp, reason);
    }
}
