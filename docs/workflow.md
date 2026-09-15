# Workflow and use cases

```mermaid
flowchart LR
  User[Warehouse user] --> CLI[ConsoleApplication]
  CLI --> P[Manage products]
  CLI --> S[Manage suppliers]
  CLI --> I[Perform stock operation]
  CLI --> R[View reorder recommendations]
  CLI --> A[View analytics]
  CLI --> G[Generate reports]
  CLI --> M[Start or stop monitor]
```

The console collects validated input. Service methods either return a result or throw a `WarehouseException`, which the console displays as an understandable error.

## Stock-out sequence

```mermaid
sequenceDiagram
  participant U as User
  participant UI as ConsoleApplication
  participant IS as InventoryService
  participant PR as ProductRepository
  participant TR as TransactionRepository
  participant DB as H2
  U->>UI: stock-out inputs
  UI->>IS: stockOut(...)
  IS->>IS: acquire per-product lock
  IS->>DB: begin JDBC transaction
  IS->>PR: findByIdForUpdate(connection, id)
  PR->>DB: SELECT ... FOR UPDATE
  alt sufficient stock
    IS->>PR: updateQuantity(connection, id, new quantity)
    PR->>DB: UPDATE PRODUCT
    IS->>TR: save(connection, StockOutTransaction)
    TR->>DB: INSERT STOCK_TRANSACTION
    IS->>DB: commit
    IS-->>UI: updated Product
  else insufficient stock or persistence error
    IS->>DB: rollback when transaction started
    IS-->>UI: WarehouseException
  end
  UI-->>U: success or error message
```

## Reorder workflow

The engine reads products, suppliers, and `STOCK_OUT` history; calculates observed daily demand and reorder point; retains products at or below the point; explains and prioritizes each recommendation; then orders output from critical to low priority.
