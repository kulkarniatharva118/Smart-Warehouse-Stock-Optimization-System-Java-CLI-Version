package com.warehouse.ui;

import com.warehouse.exception.WarehouseException;
import com.warehouse.io.CsvReportGenerator;
import com.warehouse.io.TextReportGenerator;
import com.warehouse.model.*;
import com.warehouse.service.*;
import com.warehouse.thread.InventoryMonitor;

import java.util.List;
import java.util.Scanner;

/**
 * Controller for interactive CLI workflow.
 * Demonstrates CSE2006 Unit 1 to 5 integration: Exception boundaries, Menu Loop, Validation.
 */
public class ConsoleApplication {
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final SupplierService supplierService;
    private final TransactionService transactionService;
    private final ReorderRecommendationEngine reorderEngine;
    private final AnalyticsService analyticsService;
    private final InventoryMonitor backgroundMonitor;
    private final CsvReportGenerator csvReportGenerator;
    private final TextReportGenerator textReportGenerator;
    private final Scanner scanner;

    public ConsoleApplication(ProductService productService,
                              InventoryService inventoryService,
                              SupplierService supplierService,
                              TransactionService transactionService,
                              ReorderRecommendationEngine reorderEngine,
                              AnalyticsService analyticsService,
                              InventoryMonitor backgroundMonitor) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.supplierService = supplierService;
        this.transactionService = transactionService;
        this.reorderEngine = reorderEngine;
        this.analyticsService = analyticsService;
        this.backgroundMonitor = backgroundMonitor;
        this.csvReportGenerator = new CsvReportGenerator(productService);
        this.textReportGenerator = new TextReportGenerator(transactionService, analyticsService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            try {
                ConsoleMenu.displayMainMenu(backgroundMonitor.isRunning());
                int choice = InputValidator.readInt(scanner, "Enter your choice: ");
                switch (choice) {
                    case 1 -> handleProductMenu();
                    case 2 -> handleInventoryMenu();
                    case 3 -> handleSupplierMenu();
                    case 4, 9 -> handleViewTransactions();
                    case 5 -> handleReorderRecommendations();
                    case 6 -> handleAnalytics();
                    case 7 -> handleGenerateReports();
                    case 8 -> toggleBackgroundMonitor();
                    case 0 -> {
                        exit = true;
                        if (backgroundMonitor.isRunning()) {
                            backgroundMonitor.stop();
                        }
                        System.out.println("Exiting Smart Warehouse Stock Optimization System. Goodbye!");
                    }
                    default -> System.out.println(" Invalid option. Please choose a valid menu item.");
                }
            } catch (Exception e) {
                System.out.println("\n[ERROR] An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void handleProductMenu() {
        boolean back = false;
        while (!back) {
            try {
                ConsoleMenu.displayProductMenu();
                int choice = InputValidator.readInt(scanner, "Enter choice: ");
                switch (choice) {
                    case 1 -> addProduct();
                    case 2 -> viewAllProducts();
                    case 3 -> searchProductByName();
                    case 4 -> searchProductBySku();
                    case 5 -> filterProductsByCategory();
                    case 6 -> updateProduct();
                    case 7 -> deleteProduct();
                    case 0 -> back = true;
                    default -> System.out.println(" Invalid option.");
                }
            } catch (WarehouseException e) {
                System.out.println(" [PRODUCT ERROR]: " + e.getMessage());
            }
        }
    }

    private void addProduct() throws WarehouseException {
        System.out.println("\n--- ADD NEW PRODUCT ---");
        String name = InputValidator.readNonEmptyString(scanner, "Product Name: ");
        String sku = InputValidator.readNonEmptyString(scanner, "Product SKU: ");
        String category = InputValidator.readNonEmptyString(scanner, "Category: ");
        String desc = InputValidator.readOptionalString(scanner, "Description: ");
        double price = InputValidator.readNonNegativeDouble(scanner, "Unit Price ($): ");
        int qty = InputValidator.readNonNegativeInt(scanner, "Initial Quantity: ");
        int minStock = InputValidator.readNonNegativeInt(scanner, "Minimum Stock Level: ");

        viewAllSuppliersShort();
        String suppIdStr = InputValidator.readOptionalString(scanner, "Supplier ID (Press Enter to skip): ");
        Long suppId = suppIdStr.isEmpty() ? null : Long.parseLong(suppIdStr);

        Product p = new Product(name, sku, category, desc, price, qty, minStock, suppId);
        Product saved = productService.addProduct(p);
        System.out.println(" SUCCESS: Product added with ID #" + saved.getId());
    }

    private void viewAllProducts() throws WarehouseException {
        List<Product> products = productService.getAllProducts();
        printProductList(products);
    }

    private void searchProductByName() throws WarehouseException {
        String name = InputValidator.readNonEmptyString(scanner, "Enter product name query: ");
        List<Product> products = productService.searchProductsByName(name);
        printProductList(products);
    }

    private void searchProductBySku() throws WarehouseException {
        String sku = InputValidator.readNonEmptyString(scanner, "Enter SKU: ");
        Product p = productService.getProductBySku(sku);
        System.out.println(p);
    }

    private void filterProductsByCategory() throws WarehouseException {
        String cat = InputValidator.readNonEmptyString(scanner, "Enter Category: ");
        List<Product> products = productService.getProductsByCategory(cat);
        printProductList(products);
    }

    private void updateProduct() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Enter Product ID to update: ");
        Product existing = productService.getProductById(id);

        System.out.println("Updating Product: " + existing.getName());
        String name = InputValidator.readOptionalString(scanner, "New Name (" + existing.getName() + "): ");
        String sku = InputValidator.readOptionalString(scanner, "New SKU (" + existing.getSku() + "): ");
        String cat = InputValidator.readOptionalString(scanner, "New Category (" + existing.getCategory() + "): ");
        String desc = InputValidator.readOptionalString(scanner, "New Description: ");

        if (!name.isEmpty()) existing.setName(name);
        if (!sku.isEmpty()) existing.setSku(sku);
        if (!cat.isEmpty()) existing.setCategory(cat);
        if (!desc.isEmpty()) existing.setDescription(desc);

        productService.updateProduct(existing);
        System.out.println(" SUCCESS: Product ID #" + id + " updated successfully.");
    }

    private void deleteProduct() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Enter Product ID to delete: ");
        if (productService.deleteProduct(id)) {
            System.out.println(" SUCCESS: Product ID #" + id + " deleted.");
        }
    }

    private void handleInventoryMenu() {
        boolean back = false;
        while (!back) {
            try {
                ConsoleMenu.displayInventoryMenu();
                int choice = InputValidator.readInt(scanner, "Enter choice: ");
                switch (choice) {
                    case 1 -> executeStockIn();
                    case 2 -> executeStockOut();
                    case 3 -> executeReturn();
                    case 4 -> executeAdjustment();
                    case 5 -> printProductList(inventoryService.getLowStockProducts());
                    case 6 -> printProductList(inventoryService.getOutOfStockProducts());
                    case 0 -> back = true;
                    default -> System.out.println(" Invalid option.");
                }
            } catch (WarehouseException e) {
                System.out.println(" [INVENTORY ERROR]: " + e.getMessage());
            }
        }
    }

    private void executeStockIn() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Product ID: ");
        int qty = InputValidator.readPositiveInt(scanner, "Stock In Quantity: ");
        String reason = InputValidator.readOptionalString(scanner, "Reason / Notes: ");
        String po = InputValidator.readNonEmptyString(scanner, "Purchase Order Ref: ");

        Product p = inventoryService.stockIn(id, qty, reason, po);
        System.out.println(" SUCCESS: Stock updated. New Quantity for '" + p.getName() + "' is " + p.getQuantity());
    }

    private void executeStockOut() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Product ID: ");
        int qty = InputValidator.readPositiveInt(scanner, "Stock Out Quantity: ");
        String reason = InputValidator.readOptionalString(scanner, "Reason / Notes: ");
        String so = InputValidator.readNonEmptyString(scanner, "Sales Order Ref: ");
        String dest = InputValidator.readNonEmptyString(scanner, "Destination / Warehouse Zone: ");

        Product p = inventoryService.stockOut(id, qty, reason, so, dest);
        System.out.println(" SUCCESS: Stock dispatched. Remaining Quantity for '" + p.getName() + "' is " + p.getQuantity());
    }

    private void executeReturn() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Product ID: ");
        int qty = InputValidator.readPositiveInt(scanner, "Returned Quantity: ");
        String reason = InputValidator.readOptionalString(scanner, "Notes: ");
        String retReason = InputValidator.readNonEmptyString(scanner, "Return Reason (Damaged/Defective/Excess): ");
        String source = InputValidator.readNonEmptyString(scanner, "Source (CUSTOMER/SUPPLIER): ");

        Product p = inventoryService.returnStock(id, qty, reason, retReason, source);
        System.out.println(" SUCCESS: Stock returned. New Quantity for '" + p.getName() + "' is " + p.getQuantity());
    }

    private void executeAdjustment() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Product ID: ");
        int newQty = InputValidator.readNonNegativeInt(scanner, "New Total Adjusted Quantity: ");
        String reason = InputValidator.readOptionalString(scanner, "Audit Notes: ");
        String auditor = InputValidator.readNonEmptyString(scanner, "Auditor Name: ");
        String disc = InputValidator.readNonEmptyString(scanner, "Discrepancy Reason: ");

        Product p = inventoryService.adjustStock(id, newQty, reason, auditor, disc);
        System.out.println(" SUCCESS: Stock adjusted. Updated Quantity for '" + p.getName() + "' is " + p.getQuantity());
    }

    private void handleSupplierMenu() {
        boolean back = false;
        while (!back) {
            try {
                ConsoleMenu.displaySupplierMenu();
                int choice = InputValidator.readInt(scanner, "Enter choice: ");
                switch (choice) {
                    case 1 -> addSupplier();
                    case 2 -> viewAllSuppliers();
                    case 3 -> searchSupplierByName();
                    case 4 -> updateSupplier();
                    case 5 -> deleteSupplier();
                    case 0 -> back = true;
                    default -> System.out.println(" Invalid option.");
                }
            } catch (WarehouseException e) {
                System.out.println(" [SUPPLIER ERROR]: " + e.getMessage());
            }
        }
    }

    private void addSupplier() throws WarehouseException {
        System.out.println("\n--- ADD NEW SUPPLIER ---");
        String name = InputValidator.readNonEmptyString(scanner, "Supplier Name: ");
        String contact = InputValidator.readNonEmptyString(scanner, "Contact Person: ");
        String email = InputValidator.readNonEmptyString(scanner, "Email: ");
        int leadTime = InputValidator.readPositiveInt(scanner, "Lead Time (Days): ");
        double score = InputValidator.readNonNegativeDouble(scanner, "Reliability Score (0.0 to 5.0): ");

        Supplier s = new Supplier(name, contact, email, leadTime, score);
        Supplier saved = supplierService.addSupplier(s);
        System.out.println(" SUCCESS: Supplier added with ID #" + saved.getId());
    }

    private void viewAllSuppliers() throws WarehouseException {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        if (suppliers.isEmpty()) {
            System.out.println("No suppliers found.");
        } else {
            suppliers.forEach(System.out::println);
        }
    }

    private void searchSupplierByName() throws WarehouseException {
        String name = InputValidator.readNonEmptyString(scanner, "Supplier Name Query: ");
        List<Supplier> suppliers = supplierService.searchSuppliersByName(name);
        suppliers.forEach(System.out::println);
    }

    private void updateSupplier() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Enter Supplier ID to update: ");
        Supplier existing = supplierService.getSupplierById(id);

        String name = InputValidator.readOptionalString(scanner, "New Name (" + existing.getName() + "): ");
        String contact = InputValidator.readOptionalString(scanner, "New Contact (" + existing.getContact() + "): ");
        String email = InputValidator.readOptionalString(scanner, "New Email (" + existing.getEmail() + "): ");

        if (!name.isEmpty()) existing.setName(name);
        if (!contact.isEmpty()) existing.setContact(contact);
        if (!email.isEmpty()) existing.setEmail(email);

        supplierService.updateSupplier(existing);
        System.out.println(" SUCCESS: Supplier ID #" + id + " updated.");
    }

    private void deleteSupplier() throws WarehouseException {
        long id = InputValidator.readInt(scanner, "Enter Supplier ID to delete: ");
        if (supplierService.deleteSupplier(id)) {
            System.out.println(" SUCCESS: Supplier ID #" + id + " deleted.");
        }
    }

    private void handleViewTransactions() throws WarehouseException {
        System.out.println("\n==================================================");
        System.out.println("           STOCK TRANSACTION HISTORY              ");
        System.out.println("==================================================");
        List<StockTransaction> txs = transactionService.getAllTransactions();
        List<String> details = transactionService.processPolymorphicTransactions(txs);
        details.forEach(System.out::println);
        System.out.println("==================================================");
    }

    private void handleReorderRecommendations() throws WarehouseException {
        System.out.println("\n==================================================");
        System.out.println("         SMART REORDER RECOMMENDATIONS            ");
        System.out.println("==================================================");
        List<ReorderRecommendation> recs = reorderEngine.generateRecommendations();
        if (recs.isEmpty()) {
            System.out.println("All product stock levels are optimal. No reorders needed.");
        } else {
            recs.forEach(System.out::println);
        }
        System.out.println("==================================================");
    }

    private void handleAnalytics() throws WarehouseException {
        System.out.println(analyticsService.generateAnalyticsSummaryReport());
    }

    private void handleGenerateReports() {
        try {
            String dir = "./reports";
            csvReportGenerator.generateReports(dir);
            textReportGenerator.generateReports(dir);
            System.out.println("""
                    
                     SUCCESS! All warehouse reports generated in './reports/':
                      • ./reports/inventory_report.csv
                      • ./reports/low_stock_report.csv
                      • ./reports/transaction_report.txt
                      • ./reports/warehouse_summary.txt
                    """);
        } catch (WarehouseException e) {
            System.out.println(" [REPORT ERROR]: " + e.getMessage());
        }
    }

    private void toggleBackgroundMonitor() {
        if (backgroundMonitor.isRunning()) {
            backgroundMonitor.stop();
        } else {
            backgroundMonitor.start();
        }
    }

    private void printProductList(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No matching products found.");
            return;
        }
        System.out.println("\n---------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-22s | %-10s | %-13s | %-9s | %-5s | %-8s | %s\n",
                "ID", "NAME", "SKU", "CATEGORY", "PRICE ($)", "QTY", "MIN_STK", "STATUS");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        for (Product p : products) {
            String status = p.isOutOfStock() ? " OUT OF STOCK" : (p.isLowStock() ? " LOW STOCK" : " OK");
            System.out.printf("%-4d | %-22s | %-10s | %-13s | %-9.2f | %-5d | %-8d | %s\n",
                    p.getId(), p.getName(), p.getSku(), p.getCategory(), p.getPrice(), p.getQuantity(), p.getMinimumStockLevel(), status);
        }
        System.out.println("---------------------------------------------------------------------------------------------------------\n");
    }

    private void viewAllSuppliersShort() throws WarehouseException {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        System.out.println("Available Suppliers:");
        suppliers.forEach(s -> System.out.printf("  [ID: %d] %s (Lead: %d days)\n", s.getId(), s.getName(), s.getLeadTimeDays()));
    }
}
