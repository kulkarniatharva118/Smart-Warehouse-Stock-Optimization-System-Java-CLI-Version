package com.warehouse.service;

import com.warehouse.exception.*;
import com.warehouse.model.Supplier;
import com.warehouse.repository.SupplierRepository;

import java.util.List;

/**
 * Service managing supplier records and verification.
 * Demonstrates CSE2006 Unit 2: Service Layer & Input Validation.
 */
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier addSupplier(Supplier supplier) throws WarehouseException {
        validateSupplier(supplier);
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Supplier supplier) throws WarehouseException {
        if (supplier.getId() == null) {
            throw new InvalidInputException("Supplier ID cannot be null for updates.");
        }
        getSupplierById(supplier.getId()); // Ensure exists
        validateSupplier(supplier);
        return supplierRepository.save(supplier);
    }

    public Supplier getSupplierById(Long id) throws SupplierNotFoundException, DatabaseException {
        if (id == null || id <= 0) {
            throw new SupplierNotFoundException("Supplier ID must be a positive number.");
        }
        return supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier with ID #" + id + " not found."));
    }

    public List<Supplier> getAllSuppliers() throws DatabaseException {
        return supplierRepository.findAll();
    }

    public List<Supplier> searchSuppliersByName(String name) throws DatabaseException {
        return supplierRepository.findByName(name);
    }

    public boolean deleteSupplier(Long id) throws SupplierNotFoundException, DatabaseException {
        getSupplierById(id); // Ensure exists
        return supplierRepository.deleteById(id);
    }

    private void validateSupplier(Supplier supplier) throws InvalidInputException {
        if (supplier == null) {
            throw new InvalidInputException("Supplier cannot be null.");
        }
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new InvalidInputException("Supplier name cannot be empty.");
        }
        if (supplier.getLeadTimeDays() <= 0) {
            throw new InvalidInputException("Supplier lead time days must be greater than zero.");
        }
        if (!Double.isFinite(supplier.getReliabilityScore()) || supplier.getReliabilityScore() < 0.0 || supplier.getReliabilityScore() > 5.0) {
            throw new InvalidInputException("Supplier reliability score must be between 0.0 and 5.0.");
        }
    }
}
