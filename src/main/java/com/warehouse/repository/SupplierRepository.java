package com.warehouse.repository;

import com.warehouse.database.DatabaseManager;
import com.warehouse.exception.DatabaseException;
import com.warehouse.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Supplier entities.
 * Demonstrates CSE2006 Unit 5: JDBC SQL CRUD Operations.
 */
public class SupplierRepository {

    public Supplier save(Supplier supplier) throws DatabaseException {
        if (supplier.getId() == null) {
            String sql = "INSERT INTO SUPPLIER (name, contact, email, lead_time_days, reliability_score) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, supplier.getName());
                ps.setString(2, supplier.getContact());
                ps.setString(3, supplier.getEmail());
                ps.setInt(4, supplier.getLeadTimeDays());
                ps.setDouble(5, supplier.getReliabilityScore());

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        supplier.setId(keys.getLong(1));
                    }
                }
                return supplier;
            } catch (SQLException e) {
                throw new DatabaseException("Failed to insert supplier: " + e.getMessage(), e);
            }
        } else {
            String sql = "UPDATE SUPPLIER SET name = ?, contact = ?, email = ?, lead_time_days = ?, reliability_score = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, supplier.getName());
                ps.setString(2, supplier.getContact());
                ps.setString(3, supplier.getEmail());
                ps.setInt(4, supplier.getLeadTimeDays());
                ps.setDouble(5, supplier.getReliabilityScore());
                ps.setLong(6, supplier.getId());

                ps.executeUpdate();
                return supplier;
            } catch (SQLException e) {
                throw new DatabaseException("Failed to update supplier ID #" + supplier.getId() + ": " + e.getMessage(), e);
            }
        }
    }

    public Optional<Supplier> findById(Long id) throws DatabaseException {
        String sql = "SELECT * FROM SUPPLIER WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding supplier by ID #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Supplier> findAll() throws DatabaseException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM SUPPLIER ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching suppliers: " + e.getMessage(), e);
        }
        return suppliers;
    }

    public List<Supplier> findByName(String name) throws DatabaseException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM SUPPLIER WHERE LOWER(name) LIKE LOWER(?) ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching suppliers by name '" + name + "': " + e.getMessage(), e);
        }
        return suppliers;
    }

    public boolean deleteById(Long id) throws DatabaseException {
        String sql = "DELETE FROM SUPPLIER WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting supplier ID #" + id + ": " + e.getMessage(), e);
        }
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("contact"),
                rs.getString("email"),
                rs.getInt("lead_time_days"),
                rs.getDouble("reliability_score")
        );
    }
}
