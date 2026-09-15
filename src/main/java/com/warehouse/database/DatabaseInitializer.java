package com.warehouse.database;

import com.warehouse.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initializes embedded database schema and seeds initial sample data.
 * Demonstrates CSE2006 Unit 5: DDL SQL queries, Statement execution, and data seeding.
 */
public class DatabaseInitializer {

    /**
     * Initializes schema and seeds data if empty.
     */
    public static void initializeDatabase() throws DatabaseException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create SUPPLIER table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS SUPPLIER (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        contact VARCHAR(255),
                        email VARCHAR(255),
                        lead_time_days INT NOT NULL,
                        reliability_score DOUBLE NOT NULL CHECK (reliability_score BETWEEN 0 AND 5),
                        CHECK (lead_time_days > 0)
                    );
                    """);

            // Create PRODUCT table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS PRODUCT (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        sku VARCHAR(100) NOT NULL UNIQUE,
                        category VARCHAR(100) NOT NULL,
                        description TEXT,
                        price DOUBLE NOT NULL CHECK (price >= 0),
                        quantity INT NOT NULL CHECK (quantity >= 0),
                        minimum_stock_level INT NOT NULL CHECK (minimum_stock_level >= 0),
                        supplier_id BIGINT,
                        FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(id) ON DELETE SET NULL
                    );
                    """);

            // Create STOCK_TRANSACTION table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS STOCK_TRANSACTION (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        product_id BIGINT NOT NULL,
                        type VARCHAR(50) NOT NULL,
                        quantity INT NOT NULL CHECK (quantity <> 0),
                        timestamp TIMESTAMP NOT NULL,
                        reason VARCHAR(255),
                        extra_info_1 VARCHAR(255),
                        extra_info_2 VARCHAR(255),
                        FOREIGN KEY (product_id) REFERENCES PRODUCT(id) ON DELETE CASCADE
                    );
                    """);

            // Check if sample data already exists
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PRODUCT")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedSampleData(conn);
                }
            }
        } catch (DatabaseException e) {
            throw e;
        } catch (java.sql.SQLException e) {
            throw new DatabaseException("Failed to initialize database schema or sample data: " + e.getMessage(), e);
        }
    }

    private static void seedSampleData(Connection conn) throws java.sql.SQLException {
        conn.setAutoCommit(false);
        try {
            // Seed Suppliers
            String supplierSql = "INSERT INTO SUPPLIER (name, contact, email, lead_time_days, reliability_score) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(supplierSql)) {
                addSupplierBatch(ps, "TechLogistics Inc", "John Doe", "john@techlogistics.com", 5, 4.8);
                addSupplierBatch(ps, "Global Components Co", "Jane Smith", "jane@globalcomp.com", 7, 4.5);
                addSupplierBatch(ps, "ConnectPro Supplies", "Bob Cable", "sales@connectpro.com", 3, 4.2);
                addSupplierBatch(ps, "Industrial Safety Gear", "Alice Guard", "support@safetymfg.com", 10, 3.9);
                addSupplierBatch(ps, "Packaging World", "Pack Team", "orders@packworld.com", 4, 4.7);
                ps.executeBatch();
            }

            // Seed Products
            String productSql = "INSERT INTO PRODUCT (name, sku, category, description, price, quantity, minimum_stock_level, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(productSql)) {
                addProductBatch(ps, "Wireless Mouse", "SKU-1001", "Electronics", "Ergonomic 2.4GHz wireless mouse", 25.50, 45, 15, 1L);
                addProductBatch(ps, "Mechanical Keyboard", "SKU-1002", "Electronics", "RGB tactile Mechanical Keyboard", 89.99, 8, 10, 1L); // Low stock
                addProductBatch(ps, "Barcode Scanner", "SKU-1003", "Electronics", "1D/2D Handheld Laser Scanner", 120.00, 0, 5, 1L); // Out of stock
                addProductBatch(ps, "USB Hub 4-Port", "SKU-1004", "Accessories", "High speed USB 3.0 Hub", 19.99, 60, 20, 2L);
                addProductBatch(ps, "Laptop Stand", "SKU-1005", "Accessories", "Adjustable Aluminum Laptop Stand", 34.50, 12, 15, 2L); // Low stock
                addProductBatch(ps, "Ethernet Cable 10m", "SKU-1006", "Networking", "Cat6 High-Speed LAN Cable", 12.00, 100, 25, 3L);
                addProductBatch(ps, "Thermal Printer", "SKU-1007", "Electronics", "Direct Thermal Shipping Label Printer", 210.00, 3, 5, 1L); // Low stock
                addProductBatch(ps, "Storage Drive 2TB", "SKU-1008", "Storage", "Portable External Hard Drive", 75.00, 22, 10, 1L);
                addProductBatch(ps, "Safety Gloves", "SKU-1009", "Warehouse Gear", "Heavy duty grip work gloves", 8.50, 0, 30, 4L); // Out of stock
                addProductBatch(ps, "Packaging Box Large", "SKU-1010", "Packaging", "Heavy duty cardboard box", 2.50, 500, 100, 5L);
                addProductBatch(ps, "Packing Tape Roll", "SKU-1011", "Packaging", "Heavy duty shipping tape", 4.00, 15, 50, 5L); // Low stock
                addProductBatch(ps, "Barcode Label Roll", "SKU-1012", "Packaging", "Roll of 1000 thermal shipping labels", 15.00, 80, 20, 5L);
                ps.executeBatch();
            }

            // Seed Sample Transactions
            String txSql = "INSERT INTO STOCK_TRANSACTION (product_id, type, quantity, timestamp, reason, extra_info_1, extra_info_2) VALUES (?, ?, ?, CURRENT_TIMESTAMP(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                addTxBatch(ps, 1L, "STOCK_IN", 50, "Initial shipment received", "PO-2026-001", null);
                addTxBatch(ps, 1L, "STOCK_OUT", 5, "Order dispatch", "SO-5001", "Zone A");
                addTxBatch(ps, 2L, "STOCK_IN", 20, "Restock order", "PO-2026-002", null);
                addTxBatch(ps, 2L, "STOCK_OUT", 12, "Customer sales order", "SO-5002", "Zone B");
                addTxBatch(ps, 3L, "STOCK_OUT", 10, "Warehouse clearance", "SO-5003", "Zone A");
                addTxBatch(ps, 5L, "RETURN", 2, "Customer return undamaged", "Unopened box", "CUSTOMER");
                addTxBatch(ps, 6L, "STOCK_IN", 100, "Bulk purchase", "PO-2026-003", null);
                addTxBatch(ps, 9L, "ADJUSTMENT", -5, "Damaged stock writeoff", "Auditor-John", "Physical Damage");
                ps.executeBatch();
            }

            conn.commit();
        } catch (java.sql.SQLException e) {
            try {
                conn.rollback();
            } catch (java.sql.SQLException rollbackFailure) {
                e.addSuppressed(rollbackFailure);
            }
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void addSupplierBatch(PreparedStatement ps, String name, String contact, String email, int leadTime, double score) throws java.sql.SQLException {
        ps.setString(1, name);
        ps.setString(2, contact);
        ps.setString(3, email);
        ps.setInt(4, leadTime);
        ps.setDouble(5, score);
        ps.addBatch();
    }

    private static void addProductBatch(PreparedStatement ps, String name, String sku, String cat, String desc, double price, int qty, int minStock, Long suppId) throws java.sql.SQLException {
        ps.setString(1, name);
        ps.setString(2, sku);
        ps.setString(3, cat);
        ps.setString(4, desc);
        ps.setDouble(5, price);
        ps.setInt(6, qty);
        ps.setInt(7, minStock);
        ps.setLong(8, suppId);
        ps.addBatch();
    }

    private static void addTxBatch(PreparedStatement ps, Long prodId, String type, int qty, String reason, String extra1, String extra2) throws java.sql.SQLException {
        ps.setLong(1, prodId);
        ps.setString(2, type);
        ps.setInt(3, qty);
        ps.setString(4, reason);
        ps.setString(5, extra1);
        ps.setString(6, extra2);
        ps.addBatch();
    }
}
