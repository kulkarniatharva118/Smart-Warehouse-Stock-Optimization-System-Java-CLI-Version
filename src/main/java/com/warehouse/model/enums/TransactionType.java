package com.warehouse.model.enums;

/**
 * Enum representing the type of stock transaction in the warehouse system.
 * Demonstrates CSE2006 Unit 2: Enum concepts.
 */
public enum TransactionType {
    STOCK_IN("Stock Inbound / Restock"),
    STOCK_OUT("Stock Outbound / Dispatch"),
    RETURN("Stock Return"),
    ADJUSTMENT("Stock Inventory Adjustment");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
