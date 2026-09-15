package com.warehouse.io;

import com.warehouse.exception.WarehouseException;

/**
 * Interface defining contract for warehouse report generators.
 * Demonstrates CSE2006 Unit 2: Interfaces & Unit 4: Java I/O abstraction.
 */
public interface ReportGenerator {
    /**
     * Generates report files in the target directory.
     *
     * @param directoryPath Directory path where reports will be saved.
     * @throws WarehouseException if an I/O or database error occurs.
     */
    void generateReports(String directoryPath) throws WarehouseException;
}
