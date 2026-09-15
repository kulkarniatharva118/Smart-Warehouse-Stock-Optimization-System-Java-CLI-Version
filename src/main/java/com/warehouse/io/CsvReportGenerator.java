package com.warehouse.io;

import com.warehouse.exception.DatabaseException;
import com.warehouse.exception.WarehouseException;
import com.warehouse.model.Product;
import com.warehouse.service.ProductService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Generates CSV inventory reports using Java Character I/O (BufferedWriter / Files).
 * Demonstrates CSE2006 Unit 4: Java File I/O, Character Streams, BufferedWriter.
 */
public class CsvReportGenerator implements ReportGenerator {
    private final ProductService productService;

    public CsvReportGenerator(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void generateReports(String directoryPath) throws WarehouseException {
        try {
            Path dir = createReportDirectory(directoryPath);

            List<Product> products = productService.getAllProducts();
            generateInventoryCsv(dir.resolve("inventory_report.csv"), products);
            generateLowStockCsv(dir.resolve("low_stock_report.csv"), products.stream().filter(Product::isLowStock).toList());

        } catch (IOException | DatabaseException e) {
            throw new WarehouseException("Failed to generate CSV reports: " + e.getMessage(), e);
        }
    }

    private void generateInventoryCsv(Path filePath, List<Product> products) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("ID,Name,SKU,Category,Price,Quantity,MinimumStockLevel,TotalValue,SupplierID\n");
            for (Product p : products) {
                writer.write(String.format(Locale.ROOT, "%d,\"%s\",\"%s\",\"%s\",%.2f,%d,%d,%.2f,%s\n",
                        p.getId(),
                        escapeCsv(p.getName()),
                        escapeCsv(p.getSku()),
                        escapeCsv(p.getCategory()),
                        p.getPrice(),
                        p.getQuantity(),
                        p.getMinimumStockLevel(),
                        p.getTotalValue(),
                        p.getSupplierId() != null ? p.getSupplierId() : ""));
            }
        }
    }

    private void generateLowStockCsv(Path filePath, List<Product> lowStockProducts) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("ID,Name,SKU,Category,CurrentStock,MinimumStockLevel,DeficitQuantity\n");
            for (Product p : lowStockProducts) {
                int deficit = p.getMinimumStockLevel() - p.getQuantity();
                writer.write(String.format(Locale.ROOT, "%d,\"%s\",\"%s\",\"%s\",%d,%d,%d\n",
                        p.getId(),
                        escapeCsv(p.getName()),
                        escapeCsv(p.getSku()),
                        escapeCsv(p.getCategory()),
                        p.getQuantity(),
                        p.getMinimumStockLevel(),
                        deficit));
            }
        }
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }

    private Path createReportDirectory(String directoryPath) throws IOException {
        Path directory = Paths.get(directoryPath);
        if (Files.exists(directory) && !Files.isDirectory(directory)) {
            throw new IOException("Report path is not a directory: " + directory);
        }
        return Files.createDirectories(directory);
    }
}
