package com.warehouse.thread;

import com.warehouse.model.Product;
import com.warehouse.model.ReorderRecommendation;
import com.warehouse.service.InventoryService;
import com.warehouse.service.ReorderRecommendationEngine;

import java.util.List;

/**
 * Background Inventory Monitoring thread task.
 * Periodically checks warehouse stock levels for low-stock and reorder alerts.
 * Demonstrates CSE2006 Unit 3: Multithreading, Runnable interface, thread control, and graceful shutdown.
 */
public class InventoryMonitor implements Runnable {
    private final InventoryService inventoryService;
    private final ReorderRecommendationEngine reorderEngine;
    private final long intervalMillis;
    private volatile boolean running = false;
    private Thread workerThread;

    public InventoryMonitor(InventoryService inventoryService, ReorderRecommendationEngine reorderEngine, long intervalSeconds) {
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Monitor interval must be greater than zero seconds.");
        }
        this.inventoryService = inventoryService;
        this.reorderEngine = reorderEngine;
        this.intervalMillis = intervalSeconds * 1000;
    }

    public synchronized void start() {
        if (!running) {
            running = true;
            workerThread = new Thread(this, "Inventory-Monitor-Thread");
            workerThread.setDaemon(true); // Allow JVM to exit cleanly
            workerThread.start();
            System.out.println(">>> Background Stock Monitor STARTED (Interval: " + (intervalMillis / 1000) + "s)");
        } else {
            System.out.println(">>> Background Stock Monitor is ALREADY running.");
        }
    }

    public synchronized void stop() {
        if (running) {
            running = false;
            if (workerThread != null) {
                workerThread.interrupt();
            }
            System.out.println(">>> Background Stock Monitor STOPPED.");
        } else {
            System.out.println(">>> Background Stock Monitor is NOT running.");
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    performScan();
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (com.warehouse.exception.WarehouseException e) {
                    System.err.println("[MONITOR ERROR] " + e.getMessage());
                }
            }
        } finally {
            running = false;
        }
    }

    private void performScan() throws com.warehouse.exception.WarehouseException {
        List<Product> lowStock = inventoryService.getLowStockProducts();
        List<Product> outOfStock = inventoryService.getOutOfStockProducts();
        List<ReorderRecommendation> recommendations = reorderEngine.generateRecommendations();

        System.out.println("\n--------------------------------------------------");
        System.out.println("[BACKGROUND MONITOR ALERT - " + java.time.LocalTime.now().toString().substring(0, 8) + "]");
        System.out.println("  • Low Stock Items  : " + lowStock.size());
        System.out.println("  • Out of Stock Items: " + outOfStock.size());
        System.out.println("  • Total Reorder Recommendations: " + recommendations.size());

        if (!outOfStock.isEmpty()) {
            System.out.println("  [CRITICAL OUT-OF-STOCK]: " +
                    outOfStock.stream().map(Product::getName).reduce((a, b) -> a + ", " + b).orElse(""));
        }
        System.out.println("--------------------------------------------------\n");
    }
}
