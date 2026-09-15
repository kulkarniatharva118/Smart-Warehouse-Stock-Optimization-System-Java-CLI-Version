package com.warehouse.ui;

/**
 * Utility displaying formatted CLI menus.
 * Demonstrates CSE2006 Unit 1 & Unit 4: String Formatting and Console Output.
 */
public class ConsoleMenu {

    public static void displayMainMenu(boolean monitorRunning) {
        System.out.println("""
                
                ==================================================
                 SMART WAREHOUSE STOCK OPTIMIZATION SYSTEM
                ==================================================
                 1. Product Management
                 2. Inventory Management (Stock In/Out/Return/Adjust)
                 3. Supplier Management
                 4. Stock Transactions & History
                 5. Smart Reorder Recommendations
                 6. Warehouse Analytics
                 7. Generate Reports (CSV & Text)
                 8. %s Background Stock Monitor
                 9. View Detailed Transaction History (Polymorphic)
                 0. Exit Application
                ==================================================
                """.formatted(monitorRunning ? "STOP [Running]" : "START [Stopped]"));
    }

    public static void displayProductMenu() {
        System.out.println("""
                
                --- PRODUCT MANAGEMENT ---
                 1. Add New Product
                 2. View All Products
                 3. Search Product by Name
                 4. Search Product by SKU
                 5. Filter Products by Category
                 6. Update Product Details
                 7. Delete Product
                 0. Back to Main Menu
                """);
    }

    public static void displayInventoryMenu() {
        System.out.println("""
                
                --- INVENTORY MANAGEMENT ---
                 1. Stock In (Restock)
                 2. Stock Out (Dispatch)
                 3. Return Stock (Customer/Supplier)
                 4. Adjust Stock (Audit/Discrepancy)
                 5. View Current Low-Stock Products
                 6. View Current Out-of-Stock Products
                 0. Back to Main Menu
                """);
    }

    public static void displaySupplierMenu() {
        System.out.println("""
                
                --- SUPPLIER MANAGEMENT ---
                 1. Add New Supplier
                 2. View All Suppliers
                 3. Search Supplier by Name
                 4. Update Supplier
                 5. Delete Supplier
                 0. Back to Main Menu
                """);
    }
}
