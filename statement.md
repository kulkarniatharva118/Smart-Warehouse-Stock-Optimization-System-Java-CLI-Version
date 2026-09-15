# Project Statement — Smart Warehouse Stock Optimization System

## Problem statement

Manual warehouse records can lead to inaccurate quantities, invalid stock dispatches, incomplete movement history, and delayed replenishment decisions. This project supplies a standalone console application that manages those activities with a local embedded database.

## Project scope

The system manages products, suppliers, stock-in, stock-out, returns, adjustments, transaction history, reorder recommendations, analytics, background alerts, and CSV/text reports. It is a local, single-process JDBC/H2 CLI; it does not implement authentication, web services, external APIs, multiple warehouse locations, or machine-learning forecasts.

## Target users

- Warehouse supervisors
- Inventory controllers
- Procurement staff

## Objectives

1. Persist product, supplier, and stock transaction information locally.
2. Validate inputs and prevent invalid inventory states.
3. Keep stock updates and transaction records atomic.
4. Identify replenishment needs from available history and lead time.
5. Provide reports and analytics based on stored data.

## High-level features

- Product and supplier CRUD/search
- Four stock-operation types with polymorphic transaction records
- Low-stock and out-of-stock queries
- Explainable rule-based reorder recommendations
- Analytics by category, supplier, transaction type, and movement frequency
- CSV/text reports and an optional daemon monitor

## Functional requirements

- FR-1: Add, update, list, search, and delete products and suppliers.
- FR-2: Validate SKUs, prices, quantities, supplier IDs, lead times, and reliability scores.
- FR-3: Reject stock-out quantities greater than available inventory.
- FR-4: Record every successful stock operation in the same database transaction as its stock update.
- FR-5: Calculate reorder recommendations from current stock, safety stock, supplier lead time, and stock-out history.
- FR-6: Display analytics and generate two CSV and two text reports.
- FR-7: Start and stop the background inventory monitor from the console.

## Non-functional requirements

- Usability: menu-based CLI with validation feedback.
- Reliability: custom exceptions and JDBC rollback on inventory failure.
- Concurrency: per-product synchronization plus database row locking.
- Persistence: H2 local file storage without an external server.
- Maintainability: UI, service, repository, model, database, I/O, and thread package separation.
- Portability: Maven Wrapper commands for Windows, Linux, and macOS with Java 25.
