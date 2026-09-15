# Testing

The final verified suite contains **23 JUnit 5 tests**. The last `clean test` run completed with **0 failures** and **0 errors**.

## Test approach

- **Unit/service testing:** product, supplier, inventory, reorder, and analytics tests exercise public service behavior.
- **Repository/database testing:** services use the real JDBC repositories and H2 schema, covering CRUD and persistence behavior.
- **Database isolation:** `TestDatabase.initialize()` assigns a unique in-memory H2 URL before each setup. Tests do not use the local file database or rely on test order.
- **Exception testing:** duplicate SKU, missing product/supplier, invalid values, invalid stock operations, and insufficient stock are asserted.
- **Concurrency testing:** 12 stock-out tasks compete for 10 units; the test verifies final quantity, successful/failed operations, and transaction records.
- **Rollback regression testing:** a simulated transaction-recording failure verifies that the stock update is rolled back.
- **Reorder regression testing:** tests cover healthy, threshold, low, and zero-stock products plus priority order and explanation.
- **Report testing:** tests verify files are created and CSV quoting handles commas, quotes, and line breaks.
- **Analytics testing:** normal and cleared datasets verify totals and empty collection behavior.

## Commands

```bash
# Windows
mvnw.cmd clean test

# Linux/macOS
./mvnw clean test
```
