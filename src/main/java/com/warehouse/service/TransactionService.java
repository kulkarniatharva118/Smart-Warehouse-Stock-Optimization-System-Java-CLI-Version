package com.warehouse.service;

import com.warehouse.exception.DatabaseException;
import com.warehouse.model.StockTransaction;
import com.warehouse.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing transaction logs and demonstrating polymorphic processing.
 * Demonstrates CSE2006 Unit 2 & Unit 4: Polymorphism, Collections, and Processing.
 */
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<StockTransaction> getAllTransactions() throws DatabaseException {
        return transactionRepository.findAll();
    }

    public List<StockTransaction> getTransactionsForProduct(Long productId) throws DatabaseException {
        return transactionRepository.findByProductId(productId);
    }

    /**
     * Demonstrates Polymorphism by accepting a list of abstract StockTransaction references
     * and dynamically invoking each concrete subclass's overridden getTransactionDetails() method.
     *
     * @param transactions List of StockTransaction objects
     * @return Formatted string lines summarizing transactions
     */
    public List<String> processPolymorphicTransactions(List<StockTransaction> transactions) {
        List<String> details = new ArrayList<>();
        for (StockTransaction tx : transactions) {
            // Polymorphic call: dynamically dispatches to concrete subclass implementation
            details.add(String.format("ID #%d [%s]: %s", tx.getId(), tx.getTimestamp(), tx.getTransactionDetails()));
        }
        return details;
    }
}
