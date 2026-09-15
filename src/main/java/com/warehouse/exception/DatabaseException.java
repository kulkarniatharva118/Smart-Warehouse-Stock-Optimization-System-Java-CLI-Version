package com.warehouse.exception;

/**
 * Exception thrown when a database or JDBC SQL error occurs.
 * Demonstrates CSE2006 Unit 3 & Unit 5: Custom Exception & JDBC error handling.
 */
public class DatabaseException extends WarehouseException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
