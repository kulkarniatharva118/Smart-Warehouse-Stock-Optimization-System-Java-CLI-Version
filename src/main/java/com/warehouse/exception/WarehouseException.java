package com.warehouse.exception;

/**
 * Base custom exception for the Smart Warehouse application.
 * Demonstrates CSE2006 Unit 3: Exception Handling and Custom Exceptions.
 */
public class WarehouseException extends Exception {
    public WarehouseException(String message) {
        super(message);
    }

    public WarehouseException(String message, Throwable cause) {
        super(message, cause);
    }
}
