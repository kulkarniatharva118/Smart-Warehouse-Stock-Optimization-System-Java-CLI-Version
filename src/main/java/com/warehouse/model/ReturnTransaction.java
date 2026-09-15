package com.warehouse.model;

import com.warehouse.model.enums.TransactionType;

import java.time.LocalDateTime;

/**
 * Transaction subclass representing stock return operations (customer or supplier).
 * Demonstrates CSE2006 Unit 2: Inheritance and Method Overriding.
 */
public class ReturnTransaction extends StockTransaction {
    private String returnReason;
    private String returnSource; // e.g., CUSTOMER or SUPPLIER

    public ReturnTransaction() {
        super();
        setType(TransactionType.RETURN);
    }

    public ReturnTransaction(Long id, Long productId, int quantity, LocalDateTime timestamp, String reason, String returnReason, String returnSource) {
        super(id, productId, quantity, timestamp, reason, TransactionType.RETURN);
        this.returnReason = returnReason;
        this.returnSource = returnSource;
    }

    public ReturnTransaction(Long productId, int quantity, String reason, String returnReason, String returnSource) {
        this(null, productId, quantity, LocalDateTime.now(), reason, returnReason, returnSource);
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getReturnSource() {
        return returnSource;
    }

    public void setReturnSource(String returnSource) {
        this.returnSource = returnSource;
    }

    @Override
    public String getTransactionDetails() {
        return String.format("[RETURN] Returned %d units for Product #%d from %s. Return Reason: %s. Notes: %s",
                getQuantity(), getProductId(), returnSource, returnReason, getReason());
    }
}
