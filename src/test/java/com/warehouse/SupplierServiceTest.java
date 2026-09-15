package com.warehouse;

import com.warehouse.exception.InvalidInputException;
import com.warehouse.model.Supplier;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SupplierServiceTest {
    private SupplierService supplierService;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabase.initialize();
        supplierService = new SupplierService(new SupplierRepository());
    }

    @Test
    @DisplayName("Test adding and retrieving supplier")
    void testAddAndRetrieveSupplier() throws Exception {
        Supplier supplier = new Supplier("Apex Logistics", "Mark Taylor", "mark@apex.com", 6, 4.6);
        Supplier saved = supplierService.addSupplier(supplier);

        assertNotNull(saved.getId());
        Supplier fetched = supplierService.getSupplierById(saved.getId());
        assertEquals("Apex Logistics", fetched.getName());
    }

    @Test
    @DisplayName("Test invalid reliability score (> 5.0) throws InvalidInputException")
    void testInvalidReliabilityScoreThrowsException() {
        Supplier supplier = new Supplier("Invalid Score Vendor", "Contact", "test@invalid.com", 5, 9.9);
        assertThrows(InvalidInputException.class, () -> supplierService.addSupplier(supplier));
    }

    @Test
    @DisplayName("Test listing all suppliers")
    void testListSuppliers() throws Exception {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        assertFalse(suppliers.isEmpty());
    }
}
