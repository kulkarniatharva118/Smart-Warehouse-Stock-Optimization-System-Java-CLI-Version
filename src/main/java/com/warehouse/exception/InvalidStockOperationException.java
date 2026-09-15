package com.warehouse.exception;

/**
 * Exception thrown when an illegal or negative stock adjustment quantity is specified.
 * Demonstrates CSE2006 Unit 3: Custom Exception.
 */
public class InvalidStockOperationException extends WarehouseException {
    public InvalidStockOperationException(String message) {
        super(message);
    }
}
