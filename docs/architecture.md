# Architecture

The application is a layered CLI. `Main` initializes the H2 schema, builds repositories and services, then starts `ConsoleApplication`.

```mermaid
flowchart TD
  UI[ConsoleApplication / ConsoleMenu / InputValidator] --> SL[Service layer]
  SL --> PR[ProductRepository]
  SL --> SR[SupplierRepository]
  SL --> TR[TransactionRepository]
  PR --> H2[(H2 file database)]
  SR --> H2
  TR --> H2
  IM[InventoryMonitor Runnable] --> IS[InventoryService]
  RE[ReorderRecommendationEngine] --> PR
  RE --> SR
  RE --> TR
  AS[AnalyticsService] --> PS[ProductService]
  AS --> SS[SupplierService]
  AS --> TS[TransactionService]
  RG[ReportGenerator] --> CG[CsvReportGenerator]
  RG --> TG[TextReportGenerator]
```

`InventoryService` owns stock mutation rules. `AnalyticsService`, `ReorderRecommendationEngine`, and report generators read through the service/repository components shown above. `InventoryMonitor` is a daemon thread started and stopped by the console.

```mermaid
classDiagram
  class StockTransaction { <<abstract>>
    +getTransactionDetails() String
  }
  StockTransaction <|-- StockInTransaction
  StockTransaction <|-- StockOutTransaction
  StockTransaction <|-- ReturnTransaction
  StockTransaction <|-- AdjustmentTransaction
  class ReportGenerator { <<interface>>
    +generateReports(String)
  }
  ReportGenerator <|.. CsvReportGenerator
  ReportGenerator <|.. TextReportGenerator
  ConsoleApplication --> ProductService
  ConsoleApplication --> SupplierService
  ConsoleApplication --> InventoryService
  ConsoleApplication --> TransactionService
  ConsoleApplication --> AnalyticsService
  ConsoleApplication --> ReorderRecommendationEngine
  InventoryService --> ProductRepository
  InventoryService --> TransactionRepository
```
