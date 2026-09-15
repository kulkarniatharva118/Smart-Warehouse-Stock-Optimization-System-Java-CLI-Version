package com.warehouse.exception;

/**
 * Exception thrown when user input validation fails.
 * Demonstrates CSE2006 Unit 3: Custom Exception.
 */
public class InvalidInputException extends WarehouseException {
    public InvalidInputException(String message) {
        super(message);
    }
}
