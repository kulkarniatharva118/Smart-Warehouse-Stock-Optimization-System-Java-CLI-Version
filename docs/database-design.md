# Database design

The application database is H2 at `./data/warehouse`. `DatabaseInitializer` uses `CREATE TABLE IF NOT EXISTS` and seeds data only when no products exist.

```mermaid
erDiagram
  SUPPLIER ||--o{ PRODUCT : supplies
  PRODUCT ||--o{ STOCK_TRANSACTION : records
  SUPPLIER {
    BIGINT id PK
    VARCHAR name
    VARCHAR contact
    VARCHAR email
    INT lead_time_days
    DOUBLE reliability_score
  }
  PRODUCT {
    BIGINT id PK
    VARCHAR sku UK
    VARCHAR name
    VARCHAR category
    TEXT description
    DOUBLE price
    INT quantity
    INT minimum_stock_level
    BIGINT supplier_id FK
  }
  STOCK_TRANSACTION {
    BIGINT id PK
    BIGINT product_id FK
    VARCHAR type
    INT quantity
    TIMESTAMP timestamp
    VARCHAR reason
    VARCHAR extra_info_1
    VARCHAR extra_info_2
  }
```

`PRODUCT.supplier_id` is nullable and becomes null when its supplier is deleted. Deleting a product cascades to its stock transactions. The `type` column stores the name of `TransactionType`; `extra_info_1` and `extra_info_2` hold subtype-specific details such as purchase order, sales order/destination, return details, or adjustment details.

Fresh schemas constrain non-negative product price, quantity, and minimum stock level; positive supplier lead time; reliability from 0 to 5; and non-zero transaction quantity.
