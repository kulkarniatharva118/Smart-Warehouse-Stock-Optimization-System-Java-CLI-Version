# Reorder recommendation rules

The reorder engine is deterministic and uses only current product data, supplier lead time, and recorded `STOCK_OUT` transactions.

1. Safety stock is the product's configured `minimumStockLevel`.
2. Observed daily demand is the total quantity of recorded stock-outs divided by the inclusive number of calendar days from the first to the last stock-out, rounded up. Products with no stock-out history have zero observed demand.
3. Reorder point = `(observed daily demand × supplier lead time) + safety stock`. A product without a supplier uses the documented fallback lead time of seven days.
4. A recommendation is created when current stock is at or below the reorder point. Suggested quantity replenishes to `reorder point + safety stock`.
5. Priority is `CRITICAL` for zero stock, `HIGH` at or below safety stock, `MEDIUM` below the reorder point, and `LOW` exactly at the reorder point above safety stock.

Example: a product has 20 units of recorded stock-out demand over five calendar days, a supplier lead time of three days, and safety stock of eight. Observed daily demand is four units. Its reorder point is `(4 × 3) + 8 = 20`. With 15 units on hand, the engine recommends `20 + 8 - 15 = 13` units and marks it `MEDIUM` priority.
