package com.warehouse;

import com.warehouse.io.CsvReportGenerator;
import com.warehouse.io.TextReportGenerator;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.repository.TransactionRepository;
import com.warehouse.service.AnalyticsService;
import com.warehouse.service.ProductService;
import com.warehouse.service.SupplierService;
import com.warehouse.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorTest {
    private CsvReportGenerator csvReportGenerator;
    private TextReportGenerator textReportGenerator;
    private static final String TEST_REPORTS_DIR = "./target/test-reports";

    @BeforeEach
    void setUp() throws Exception {
        TestDatabase.initialize();
        ProductRepository prodRepo = new ProductRepository();
        SupplierRepository suppRepo = new SupplierRepository();
        TransactionRepository txRepo = new TransactionRepository();

        ProductService prodService = new ProductService(prodRepo, suppRepo);
        SupplierService suppService = new SupplierService(suppRepo);
        TransactionService txService = new TransactionService(txRepo);
        AnalyticsService analyticsService = new AnalyticsService(prodService, suppService, txService);

        csvReportGenerator = new CsvReportGenerator(prodService);
        textReportGenerator = new TextReportGenerator(txService, analyticsService);
    }

    @Test
    @DisplayName("Test CSV and Text report generation creates physical files")
    void testReportGenerationFilesCreated() throws Exception {
        csvReportGenerator.generateReports(TEST_REPORTS_DIR);
        textReportGenerator.generateReports(TEST_REPORTS_DIR);

        File invCsv = new File(TEST_REPORTS_DIR + "/inventory_report.csv");
        File lowCsv = new File(TEST_REPORTS_DIR + "/low_stock_report.csv");
        File txTxt = new File(TEST_REPORTS_DIR + "/transaction_report.txt");
        File summaryTxt = new File(TEST_REPORTS_DIR + "/warehouse_summary.txt");

        assertTrue(invCsv.exists() && invCsv.length() > 0, "inventory_report.csv should be created and non-empty");
        assertTrue(lowCsv.exists() && lowCsv.length() > 0, "low_stock_report.csv should be created and non-empty");
        assertTrue(txTxt.exists() && txTxt.length() > 0, "transaction_report.txt should be created and non-empty");
        assertTrue(summaryTxt.exists() && summaryTxt.length() > 0, "warehouse_summary.txt should be created and non-empty");
    }

    @Test
    @DisplayName("CSV reports escape commas, quotes, and line breaks")
    void testCsvEscaping() throws Exception {
        ProductService productService = new ProductService(new ProductRepository(), new SupplierRepository());
        productService.addProduct(new com.warehouse.model.Product("Quoted, \"Product\"\nName", "CSV-ESCAPE", "Test, Category", "", 1.0, 1, 0, 1L));
        Path reportDirectory = Path.of(TEST_REPORTS_DIR, "escaping");
        new CsvReportGenerator(productService).generateReports(reportDirectory.toString());
        String csv = Files.readString(reportDirectory.resolve("inventory_report.csv"));
        assertTrue(csv.contains("\"Quoted, \"\"Product\"\"\nName\""));
        assertTrue(csv.contains("\"Test, Category\""));
    }
}
