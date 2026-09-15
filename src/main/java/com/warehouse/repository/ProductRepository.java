package com.warehouse.repository;

import com.warehouse.database.DatabaseManager;
import com.warehouse.exception.DatabaseException;
import com.warehouse.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) / Repository for Product entities.
 * Demonstrates CSE2006 Unit 5: JDBC PreparedStatement, ResultSet, and CRUD SQL Operations.
 */
public class ProductRepository {

    public Product save(Product product) throws DatabaseException {
        if (product.getId() == null) {
            String sql = "INSERT INTO PRODUCT (name, sku, category, description, price, quantity, minimum_stock_level, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, product.getName());
                ps.setString(2, product.getSku());
                ps.setString(3, product.getCategory());
                ps.setString(4, product.getDescription());
                ps.setDouble(5, product.getPrice());
                ps.setInt(6, product.getQuantity());
                ps.setInt(7, product.getMinimumStockLevel());
                if (product.getSupplierId() != null) {
                    ps.setLong(8, product.getSupplierId());
                } else {
                    ps.setNull(8, Types.BIGINT);
                }

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setId(keys.getLong(1));
                    }
                }
                return product;
            } catch (SQLException e) {
                throw new DatabaseException("Failed to insert product: " + e.getMessage(), e);
            }
        } else {
            String sql = "UPDATE PRODUCT SET name = ?, sku = ?, category = ?, description = ?, price = ?, quantity = ?, minimum_stock_level = ?, supplier_id = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, product.getName());
                ps.setString(2, product.getSku());
                ps.setString(3, product.getCategory());
                ps.setString(4, product.getDescription());
                ps.setDouble(5, product.getPrice());
                ps.setInt(6, product.getQuantity());
                ps.setInt(7, product.getMinimumStockLevel());
                if (product.getSupplierId() != null) {
                    ps.setLong(8, product.getSupplierId());
                } else {
                    ps.setNull(8, Types.BIGINT);
                }
                ps.setLong(9, product.getId());

                ps.executeUpdate();
                return product;
            } catch (SQLException e) {
                throw new DatabaseException("Failed to update product ID #" + product.getId() + ": " + e.getMessage(), e);
            }
        }
    }

    public Optional<Product> findById(Long id) throws DatabaseException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            return findById(conn, id, false);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by ID #" + id + ": " + e.getMessage(), e);
        }
    }

    public Optional<Product> findByIdForUpdate(Connection conn, Long id) throws DatabaseException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return findById(conn, id, true);
    }

    private Optional<Product> findById(Connection conn, Long id, boolean forUpdate) throws DatabaseException {
        String sql = "SELECT * FROM PRODUCT WHERE id = ?" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by ID #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Product> findBySku(String sku) throws DatabaseException {
        String sql = "SELECT * FROM PRODUCT WHERE LOWER(sku) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by SKU '" + sku + "': " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Product> findAll() throws DatabaseException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCT ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all products: " + e.getMessage(), e);
        }
        return products;
    }

    public List<Product> findByName(String name) throws DatabaseException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCT WHERE LOWER(name) LIKE LOWER(?) ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching products by name '" + name + "': " + e.getMessage(), e);
        }
        return products;
    }

    public List<Product> findByCategory(String category) throws DatabaseException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCT WHERE LOWER(category) = LOWER(?) ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding products by category '" + category + "': " + e.getMessage(), e);
        }
        return products;
    }

    public boolean deleteById(Long id) throws DatabaseException {
        String sql = "DELETE FROM PRODUCT WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting product ID #" + id + ": " + e.getMessage(), e);
        }
    }

    public void updateQuantity(Long productId, int newQuantity) throws DatabaseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            updateQuantity(conn, productId, newQuantity);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating quantity for product ID #" + productId + ": " + e.getMessage(), e);
        }
    }

    public void updateQuantity(Connection conn, Long productId, int newQuantity) throws DatabaseException {
        if (newQuantity < 0) {
            throw new DatabaseException("Product quantity cannot be negative.");
        }
        String sql = "UPDATE PRODUCT SET quantity = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setLong(2, productId);
            if (ps.executeUpdate() != 1) {
                throw new DatabaseException("Product ID #" + productId + " was not found while updating quantity.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error updating quantity for product ID #" + productId + ": " + e.getMessage(), e);
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Long supplierId = rs.getLong("supplier_id");
        if (rs.wasNull()) {
            supplierId = null;
        }
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("sku"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                rs.getInt("minimum_stock_level"),
                supplierId
        );
    }
}
