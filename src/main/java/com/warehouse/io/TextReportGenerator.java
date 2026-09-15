package com.warehouse.io;

import com.warehouse.exception.DatabaseException;
import com.warehouse.exception.WarehouseException;
import com.warehouse.model.StockTransaction;
import com.warehouse.service.AnalyticsService;
import com.warehouse.service.TransactionService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Generates formatted text reports using Java Character I/O (BufferedWriter / Files).
 * Demonstrates CSE2006 Unit 4: Java File I/O, Character Streams.
 */
public class TextReportGenerator implements ReportGenerator {
    private final TransactionService transactionService;
    private final AnalyticsService analyticsService;

    public TextReportGenerator(TransactionService transactionService, AnalyticsService analyticsService) {
        this.transactionService = transactionService;
        this.analyticsService = analyticsService;
    }

    @Override
    public void generateReports(String directoryPath) throws WarehouseException {
        try {
            Path dir = createReportDirectory(directoryPath);

            generateTransactionReport(dir.resolve("transaction_report.txt"), transactionService.getAllTransactions());
            generateSummaryReport(dir.resolve("warehouse_summary.txt"), analyticsService.generateAnalyticsSummaryReport());

        } catch (IOException | DatabaseException e) {
            throw new WarehouseException("Failed to generate text reports: " + e.getMessage(), e);
        }
    }

    private void generateTransactionReport(Path filePath, List<StockTransaction> transactions) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("====================================================================================================\n");
            writer.write("                                WAREHOUSE STOCK TRANSACTIONS REPORT                                  \n");
            writer.write("====================================================================================================\n");
            writer.write(String.format("%-6s | %-20s | %-12s | %-10s | %-8s | %s\n",
                    "TX_ID", "TIMESTAMP", "TYPE", "PROD_ID", "QTY", "DETAILS"));
            writer.write("----------------------------------------------------------------------------------------------------\n");

            if (transactions.isEmpty()) {
                writer.write("No stock transactions recorded.\n");
            }
            for (StockTransaction tx : transactions) {
                writer.write(String.format("%-6d | %-20s | %-12s | %-10d | %-8d | %s\n",
                        tx.getId(),
                        tx.getTimestamp().toString().replace('T', ' ').substring(0, 19),
                        tx.getType(),
                        tx.getProductId(),
                        tx.getQuantity(),
                        tx.getTransactionDetails()));
            }
            writer.write("====================================================================================================\n");
        }
    }

    private void generateSummaryReport(Path filePath, String summaryText) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(summaryText);
        }
    }

    private Path createReportDirectory(String directoryPath) throws IOException {
        Path directory = Paths.get(directoryPath);
        if (Files.exists(directory) && !Files.isDirectory(directory)) {
            throw new IOException("Report path is not a directory: " + directory);
        }
        return Files.createDirectories(directory);
    }
}
