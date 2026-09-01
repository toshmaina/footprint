#!/usr/bin/env bash
# =====================================================================
# Baobab Distributors — Section 5 acceptance test, for real.
# 10 units of a SKU, 50 concurrent 1-unit order attempts, exactly 10
# should succeed (reserved), 0 should be oversold, 40 should backorder.
# =====================================================================
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ACCESS_TOKEN="${ACCESS_TOKEN:?Set ACCESS_TOKEN to a valid admin token first}"
CUSTOMER_ID="${CUSTOMER_ID:?Set CUSTOMER_ID to a customer whose default warehouse holds the test stock}"
PRODUCT_ID="${PRODUCT_ID:?Set PRODUCT_ID to the SKUs product_id (should have exactly 10 available)}"

echo "Firing 50 concurrent 1-unit orders for product $PRODUCT_ID..."

pids=()
outdir=$(mktemp -d)

for i in $(seq 1 50); do
  (
    curl -s -o "$outdir/resp_$i.json" -w "%{http_code}" \
      -X POST "$BASE_URL/orders" \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"customer_id\": $CUSTOMER_ID, \"lines\": [{\"product_id\": $PRODUCT_ID, \"quantity_ordered\": 1}]}" \
      > "$outdir/status_$i.txt"
  ) &
  pids+=($!)
done

for pid in "${pids[@]}"; do
  wait "$pid"
done

echo ""
echo "All 50 requests completed. Analyzing results..."

reserved_count=$(grep -l '"status":"reserved"' "$outdir"/resp_*.json 2>/dev/null | wc -l) || true
backordered_count=$(grep -l '"status":"backordered"' "$outdir"/resp_*.json 2>/dev/null | wc -l) || true

echo "Reserved:    $reserved_count  (expected: 10)"
echo "Backordered: $backordered_count  (expected: 40)"

if [ "$reserved_count" -eq 10 ] && [ "$backordered_count" -eq 40 ]; then
  echo "PASS: exactly 10 reserved, 0 oversold."
  rm -rf "$outdir"
else
  echo "FAIL: counts don't match expected 10/40 split - inspect $outdir for individual responses."
fi

echo ""
echo "IMPORTANT: available_stock() reports raw physical ledger availability,"
echo "which correctly STAYS AT 10 here - reservations deliberately don't touch"
echo "that ledger (only PICK/SHIP do). The number that actually hits 0 is"
echo "Available-to-Promise (available_stock minus active reservations), which"
echo "reserve_stock() computes internally but isn't currently exposed as its"
echo "own endpoint. To see it directly:"
echo ""
echo "SELECT available_stock(\$PRODUCT_ID, \$WAREHOUSE_ID)"
echo "     - COALESCE(SUM(stock_reservation_quantity), 0)"
echo "FROM stock_reservations"
echo "WHERE product_id = \$PRODUCT_ID AND warehouse_id = \$WAREHOUSE_ID"
echo "  AND stock_reservation_status = 'active';"
echo ""
echo "This should return 0 after this test - that's the actual acceptance"
echo "criterion, not the stock report endpoint's raw number."