package com.warehouse.exception;

/**
 * Exception thrown when attempting to add a product with a duplicate SKU.
 * Demonstrates CSE2006 Unit 3: Custom Exception.
 */
public class DuplicateSkuException extends WarehouseException {
    public DuplicateSkuException(String message) {
        super(message);
    }
}
