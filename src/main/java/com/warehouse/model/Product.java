package com.warehouse.model;

import java.util.Objects;

/**
 * Product domain entity representing a stock item in the warehouse.
 * Demonstrates CSE2006 Unit 2: Classes, Objects, Encapsulation, Constructors, Getters/Setters.
 */
public class Product {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private String description;
    private double price;
    private int quantity;
    private int minimumStockLevel;
    private Long supplierId;

    public Product() {
    }

    public Product(Long id, String name, String sku, String category, String description,
                   double price, int quantity, int minimumStockLevel, Long supplierId) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.minimumStockLevel = minimumStockLevel;
        this.supplierId = supplierId;
    }

    public Product(String name, String sku, String category, String description,
                   double price, int quantity, int minimumStockLevel, Long supplierId) {
        this(null, name, sku, category, description, price, quantity, minimumStockLevel, supplierId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public void setMinimumStockLevel(int minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public double getTotalValue() {
        return this.price * this.quantity;
    }

    public boolean isLowStock() {
        return this.quantity <= this.minimumStockLevel && this.quantity > 0;
    }

    public boolean isOutOfStock() {
        return this.quantity <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        if (id != null && product.id != null) {
            return id.equals(product.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return String.format("Product[ID=%d, Name='%s', SKU='%s', Category='%s', Price=$%.2f, Qty=%d, MinStock=%d, SupplierID=%d]",
                id, name, sku, category, price, quantity, minimumStockLevel, supplierId);
    }
}
