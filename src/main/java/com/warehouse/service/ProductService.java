package com.warehouse.service;

import com.warehouse.exception.*;
import com.warehouse.model.Product;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service managing product catalog and validation business logic.
 * Demonstrates CSE2006 Unit 2 & Unit 3: Encapsulation, Exception Handling, Collections.
 */
public class ProductService {
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    public Product addProduct(Product product) throws WarehouseException {
        validateProduct(product);
        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new DuplicateSkuException("Product with SKU '" + product.getSku() + "' already exists.");
        }
        if (product.getSupplierId() != null && supplierRepository.findById(product.getSupplierId()).isEmpty()) {
            throw new SupplierNotFoundException("Associated Supplier ID #" + product.getSupplierId() + " does not exist.");
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Product product) throws WarehouseException {
        if (product.getId() == null) {
            throw new InvalidInputException("Product ID cannot be null for updates.");
        }
        validateProduct(product);

        Optional<Product> existingWithSku = productRepository.findBySku(product.getSku());
        if (existingWithSku.isPresent() && !existingWithSku.get().getId().equals(product.getId())) {
            throw new DuplicateSkuException("Another product already exists with SKU '" + product.getSku() + "'.");
        }
        if (product.getSupplierId() != null && supplierRepository.findById(product.getSupplierId()).isEmpty()) {
            throw new SupplierNotFoundException("Associated Supplier ID #" + product.getSupplierId() + " does not exist.");
        }

        return productRepository.save(product);
    }

    public Product getProductById(Long id) throws ProductNotFoundException, DatabaseException {
        if (id == null || id <= 0) {
            throw new ProductNotFoundException("Product ID must be a positive number.");
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID #" + id + " not found."));
    }

    public Product getProductBySku(String sku) throws ProductNotFoundException, DatabaseException {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product with SKU '" + sku + "' not found."));
    }

    public List<Product> getAllProducts() throws DatabaseException {
        return productRepository.findAll();
    }

    public List<Product> searchProductsByName(String name) throws DatabaseException {
        return productRepository.findByName(name);
    }

    public List<Product> getProductsByCategory(String category) throws DatabaseException {
        return productRepository.findByCategory(category);
    }

    public boolean deleteProduct(Long id) throws ProductNotFoundException, DatabaseException {
        getProductById(id); // Ensure product exists
        return productRepository.deleteById(id);
    }

    private void validateProduct(Product product) throws InvalidInputException {
        if (product == null) {
            throw new InvalidInputException("Product object cannot be null.");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new InvalidInputException("Product name cannot be empty.");
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new InvalidInputException("Product SKU cannot be empty.");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new InvalidInputException("Product category cannot be empty.");
        }
        if (!Double.isFinite(product.getPrice()) || product.getPrice() < 0) {
            throw new InvalidInputException("Product price must be a finite, non-negative value.");
        }
        if (product.getQuantity() < 0) {
            throw new InvalidInputException("Product initial quantity cannot be negative.");
        }
        if (product.getMinimumStockLevel() < 0) {
            throw new InvalidInputException("Product minimum stock level cannot be negative.");
        }
        if (product.getSupplierId() != null && product.getSupplierId() <= 0) {
            throw new InvalidInputException("Supplier ID must be a positive number.");
        }
    }
}
