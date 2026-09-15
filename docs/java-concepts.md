# CSE2006 concept mapping

| CSE2006 concept | Actual class/file | How it is used |
|---|---|---|
| Java fundamentals | `ui/InputValidator.java`, `ui/ConsoleApplication.java` | methods, loops, conditions, parsing, and menu switch expressions |
| OOP | `model/Product.java`, `model/Supplier.java` | domain classes represent catalog entities |
| Encapsulation | `Product`, `Supplier`, `StockTransaction` | private state accessed through methods |
| Constructors | model classes and transaction subclasses | initialize new and database-mapped objects |
| Inheritance | transaction subclasses | specialize `StockTransaction` |
| Method overriding | four transaction subclasses | override `getTransactionDetails()` |
| Runtime polymorphism | `service/TransactionService.java` | calls overridden details through `List<StockTransaction>` |
| Abstract classes | `model/StockTransaction.java` | common stock transaction contract |
| Interfaces | `io/ReportGenerator.java` | common report-generation operation |
| Enums | `TransactionType`, `ReorderPriority` | type-safe classifications |
| Exceptions / custom exceptions | `exception/WarehouseException.java` and subclasses | validation, not-found, duplicate, stock, and database failures |
| Multithreading | `thread/InventoryMonitor.java` | `Runnable` daemon performs periodic scans |
| Synchronization | `service/InventoryService.java` | per-product Java locks plus row locking |
| Collections | analytics, reorder engine, transaction service | `List`, `Map`, `EnumMap`, `ConcurrentHashMap` |
| Streams | analytics and reorder engine | grouping, counting, mapping, filtering, sorting, reductions |
| I/O | report generators | `Path`, `Files`, and `BufferedWriter` |
| JDBC | database manager, initializer, repositories | H2 connections, SQL, prepared statements, results, transactions |

JPA is not implemented. The project intentionally persists data using JDBC.
