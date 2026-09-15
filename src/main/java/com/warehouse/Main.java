package com.warehouse;

import com.warehouse.database.DatabaseInitializer;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.repository.TransactionRepository;
import com.warehouse.service.*;
import com.warehouse.thread.InventoryMonitor;
import com.warehouse.ui.ConsoleApplication;

/**
 * Main Entry Point for the Smart Warehouse Stock Optimization System.
 * Demonstrates CSE2006 Programming in Java Course Requirements.
 *
 * CSE2006 Unit Summary:
 * - Unit 1: Fundamentals, loops, methods, conditions, variables.
 * - Unit 2: OOP classes, encapsulation, inheritance (StockTransaction hierarchy), polymorphism, abstract classes, interfaces (ReportGenerator), enums (TransactionType, ReorderPriority).
 * - Unit 3: Exception Handling (Custom exceptions), Multithreading (InventoryMonitor daemon thread), Synchronization (InventoryService per-product thread locks).
 * - Unit 4: Collections (List, Map, Set), Streams (groupingBy, mapping, filtering), File I/O (BufferedWriter, Path, Files).
 * - Unit 5: JDBC Embedded H2 Database (Connection, Statement, PreparedStatement, ResultSet, transaction commit/rollback).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Initializing Smart Warehouse System...");

        try {
            // 1. Initialize embedded H2 database schema and seed sample data
            DatabaseInitializer.initializeDatabase();

            // 2. Instantiate Repositories (Data Access Layer)
            ProductRepository productRepository = new ProductRepository();
            SupplierRepository supplierRepository = new SupplierRepository();
            TransactionRepository transactionRepository = new TransactionRepository();

            // 3. Instantiate Business Services
            ProductService productService = new ProductService(productRepository, supplierRepository);
            SupplierService supplierService = new SupplierService(supplierRepository);
            InventoryService inventoryService = new InventoryService(productRepository, transactionRepository);
            TransactionService transactionService = new TransactionService(transactionRepository);
            ReorderRecommendationEngine reorderEngine = new ReorderRecommendationEngine(productRepository, supplierRepository, transactionRepository);
            AnalyticsService analyticsService = new AnalyticsService(productService, supplierService, transactionService);

            // 4. Instantiate Multithreaded Background Stock Monitor
            InventoryMonitor backgroundMonitor = new InventoryMonitor(inventoryService, reorderEngine, 10);

            // 5. Start Console Application UI
            ConsoleApplication app = new ConsoleApplication(
                    productService,
                    inventoryService,
                    supplierService,
                    transactionService,
                    reorderEngine,
                    analyticsService,
                    backgroundMonitor
            );

            app.start();

        } catch (Exception e) {
            System.err.println("Fatal Application Startup Failure: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
