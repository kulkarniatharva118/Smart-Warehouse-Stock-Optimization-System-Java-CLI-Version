package com.warehouse;

import com.warehouse.database.DatabaseInitializer;

import java.util.UUID;
import com.warehouse.database.DatabaseManager;
import java.sql.Connection;
import java.sql.Statement;

/** Creates a fresh in-memory H2 database for every test setup. */
final class TestDatabase {
    private TestDatabase() { }

    static void initialize() throws Exception {
        System.setProperty("warehouse.db.url",
                "jdbc:h2:mem:warehouse_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
        DatabaseInitializer.initializeDatabase();
    }

    static void clear() throws Exception {
        try (Connection connection = DatabaseManager.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM STOCK_TRANSACTION");
            statement.executeUpdate("DELETE FROM PRODUCT");
            statement.executeUpdate("DELETE FROM SUPPLIER");
        }
    }
}
