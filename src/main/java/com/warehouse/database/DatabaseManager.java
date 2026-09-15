package com.warehouse.database;

import com.warehouse.exception.DatabaseException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connections to the embedded H2 database.
 * Demonstrates CSE2006 Unit 5: JDBC Database Connection & Management.
 * Uses local file storage at ./data/warehouse so data persists between runs.
 */
public class DatabaseManager {
    private static final String DB_DIR = "./data";
    private static final String DEFAULT_DB_URL = "jdbc:h2:file:./data/warehouse;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
            File dir = new File(DB_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Unable to create database directory: " + dir.getAbsolutePath());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 Database driver not found in classpath", e);
        }
    }

    /**
     * Obtains a standard JDBC connection to the embedded H2 database.
     *
     * @return Connection object
     * @throws DatabaseException if connection creation fails
     */
    public static Connection getConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(System.getProperty("warehouse.db.url", DEFAULT_DB_URL), DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish H2 database connection: " + e.getMessage(), e);
        }
    }
}
