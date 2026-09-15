package com.warehouse.model;

import com.warehouse.model.enums.TransactionType;

import java.time.LocalDateTime;

/**
 * Transaction subclass representing outbound stock dispatch operations.
 * Demonstrates CSE2006 Unit 2: Inheritance and Method Overriding.
 */
public class StockOutTransaction extends StockTransaction {
    private String salesOrderNumber;
    private String destination;

    public StockOutTransaction() {
        super();
        setType(TransactionType.STOCK_OUT);
    }

    public StockOutTransaction(Long id, Long productId, int quantity, LocalDateTime timestamp, String reason, String salesOrderNumber, String destination) {
        super(id, productId, quantity, timestamp, reason, TransactionType.STOCK_OUT);
        this.salesOrderNumber = salesOrderNumber;
        this.destination = destination;
    }

    public StockOutTransaction(Long productId, int quantity, String reason, String salesOrderNumber, String destination) {
        this(null, productId, quantity, LocalDateTime.now(), reason, salesOrderNumber, destination);
    }

    public String getSalesOrderNumber() {
        return salesOrderNumber;
    }

    public void setSalesOrderNumber(String salesOrderNumber) {
        this.salesOrderNumber = salesOrderNumber;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String getTransactionDetails() {
        return String.format("[STOCK-OUT] Dispatched %d units for Product #%d to %s. SO Ref: %s. Reason: %s",
                getQuantity(), getProductId(), destination, salesOrderNumber, getReason());
    }
}
