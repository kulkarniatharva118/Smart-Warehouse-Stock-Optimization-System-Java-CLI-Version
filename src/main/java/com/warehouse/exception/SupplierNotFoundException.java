package com.warehouse.exception;

/**
 * Exception thrown when a requested supplier is not found in the system.
 * Demonstrates CSE2006 Unit 3: Exception Handling.
 */
public class SupplierNotFoundException extends WarehouseException {
    public SupplierNotFoundException(String message) {
        super(message);
    }
}
