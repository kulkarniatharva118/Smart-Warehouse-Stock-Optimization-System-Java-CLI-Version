package com.warehouse.model;

import com.warehouse.model.enums.TransactionType;

import java.time.LocalDateTime;

/**
 * Transaction subclass representing stock adjustment operations (audit reconciliations, damage write-offs).
 * Demonstrates CSE2006 Unit 2: Inheritance and Method Overriding.
 */
public class AdjustmentTransaction extends StockTransaction {
    private String adjustedBy;
    private String discrepancyReason;

    public AdjustmentTransaction() {
        super();
        setType(TransactionType.ADJUSTMENT);
    }

    public AdjustmentTransaction(Long id, Long productId, int quantity, LocalDateTime timestamp, String reason, String adjustedBy, String discrepancyReason) {
        super(id, productId, quantity, timestamp, reason, TransactionType.ADJUSTMENT);
        this.adjustedBy = adjustedBy;
        this.discrepancyReason = discrepancyReason;
    }

    public AdjustmentTransaction(Long productId, int quantity, String reason, String adjustedBy, String discrepancyReason) {
        this(null, productId, quantity, LocalDateTime.now(), reason, adjustedBy, discrepancyReason);
    }

    public String getAdjustedBy() {
        return adjustedBy;
    }

    public void setAdjustedBy(String adjustedBy) {
        this.adjustedBy = adjustedBy;
    }

    public String getDiscrepancyReason() {
        return discrepancyReason;
    }

    public void setDiscrepancyReason(String discrepancyReason) {
        this.discrepancyReason = discrepancyReason;
    }

    @Override
    public String getTransactionDetails() {
        return String.format("[ADJUSTMENT] Adjusted stock by %d units for Product #%d by Auditor %s. Discrepancy: %s. Reason: %s",
                getQuantity(), getProductId(), adjustedBy, discrepancyReason, getReason());
    }
}
