package com.warehouse.exception;

/**
 * Exception thrown when a requested product cannot be found in the system.
 * Demonstrates CSE2006 Unit 3: Exception Handling.
 */
public class ProductNotFoundException extends WarehouseException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
