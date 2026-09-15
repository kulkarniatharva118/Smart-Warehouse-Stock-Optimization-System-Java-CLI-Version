package com.warehouse.exception;

/**
 * Exception thrown when a stock-out or return operation exceeds available stock.
 * Demonstrates CSE2006 Unit 3: Custom Exception.
 */
public class InsufficientStockException extends WarehouseException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
