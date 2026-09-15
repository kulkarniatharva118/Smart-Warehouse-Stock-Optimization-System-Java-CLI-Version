package com.warehouse.model;

import com.warehouse.model.enums.TransactionType;

import java.time.LocalDateTime;

/**
 * Transaction subclass representing inbound stock restock operations.
 * Demonstrates CSE2006 Unit 2: Inheritance and Method Overriding.
 */
public class StockInTransaction extends StockTransaction {
    private String purchaseOrderNumber;

    public StockInTransaction() {
        super();
        setType(TransactionType.STOCK_IN);
    }

    public StockInTransaction(Long id, Long productId, int quantity, LocalDateTime timestamp, String reason, String purchaseOrderNumber) {
        super(id, productId, quantity, timestamp, reason, TransactionType.STOCK_IN);
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public StockInTransaction(Long productId, int quantity, String reason, String purchaseOrderNumber) {
        this(null, productId, quantity, LocalDateTime.now(), reason, purchaseOrderNumber);
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    @Override
    public String getTransactionDetails() {
        return String.format("[STOCK-IN] Restocked %d units for Product #%d. PO Ref: %s. Reason: %s",
                getQuantity(), getProductId(), purchaseOrderNumber, getReason());
    }
}
