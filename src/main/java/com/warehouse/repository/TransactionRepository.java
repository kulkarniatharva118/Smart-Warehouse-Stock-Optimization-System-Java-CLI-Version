package com.warehouse.repository;

import com.warehouse.database.DatabaseManager;
import com.warehouse.exception.DatabaseException;
import com.warehouse.model.*;
import com.warehouse.model.enums.TransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for persisting and retrieving StockTransaction objects.
 * Demonstrates CSE2006 Unit 2 & Unit 5: Polymorphic Object Persistence using JDBC.
 */
public class TransactionRepository {

    public StockTransaction save(StockTransaction transaction) throws DatabaseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return save(conn, transaction);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save stock transaction: " + e.getMessage(), e);
        }
    }

    /** Saves a transaction in the caller's JDBC transaction. */
    public StockTransaction save(Connection conn, StockTransaction transaction) throws DatabaseException {
        String sql = "INSERT INTO STOCK_TRANSACTION (product_id, type, quantity, timestamp, reason, extra_info_1, extra_info_2) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, transaction.getProductId());
            ps.setString(2, transaction.getType().name());
            ps.setInt(3, transaction.getQuantity());
            ps.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
            ps.setString(5, transaction.getReason());

            if (transaction instanceof StockInTransaction inTx) {
                ps.setString(6, inTx.getPurchaseOrderNumber());
                ps.setNull(7, Types.VARCHAR);
            } else if (transaction instanceof StockOutTransaction outTx) {
                ps.setString(6, outTx.getSalesOrderNumber());
                ps.setString(7, outTx.getDestination());
            } else if (transaction instanceof ReturnTransaction returnTx) {
                ps.setString(6, returnTx.getReturnReason());
                ps.setString(7, returnTx.getReturnSource());
            } else if (transaction instanceof AdjustmentTransaction adjTx) {
                ps.setString(6, adjTx.getAdjustedBy());
                ps.setString(7, adjTx.getDiscrepancyReason());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setId(keys.getLong(1));
                }
            }
            return transaction;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save stock transaction: " + e.getMessage(), e);
        }
    }

    public List<StockTransaction> findAll() throws DatabaseException {
        List<StockTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM STOCK_TRANSACTION ORDER BY timestamp DESC, id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving transaction history: " + e.getMessage(), e);
        }
        return transactions;
    }

    public List<StockTransaction> findByProductId(Long productId) throws DatabaseException {
        List<StockTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM STOCK_TRANSACTION WHERE product_id = ? ORDER BY timestamp DESC, id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving transactions for product ID #" + productId + ": " + e.getMessage(), e);
        }
        return transactions;
    }

    private StockTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long productId = rs.getLong("product_id");
        String typeStr = rs.getString("type");
        int quantity = rs.getInt("quantity");
        Timestamp ts = rs.getTimestamp("timestamp");
        LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        String reason = rs.getString("reason");
        String extra1 = rs.getString("extra_info_1");
        String extra2 = rs.getString("extra_info_2");

        TransactionType type = TransactionType.valueOf(typeStr);

        return switch (type) {
            case STOCK_IN -> new StockInTransaction(id, productId, quantity, timestamp, reason, extra1);
            case STOCK_OUT -> new StockOutTransaction(id, productId, quantity, timestamp, reason, extra1, extra2);
            case RETURN -> new ReturnTransaction(id, productId, quantity, timestamp, reason, extra1, extra2);
            case ADJUSTMENT -> new AdjustmentTransaction(id, productId, quantity, timestamp, reason, extra1, extra2);
        };
    }
}
