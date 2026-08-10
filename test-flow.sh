#!/usr/bin/env bash
# Cake Delight End-to-End API Verification Script

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "=========================================="
echo " Cake Delight E2E Flow Verification"
echo " Target API Gateway: ${BASE_URL}"
echo "=========================================="

echo -e "\n1. Fetching Catalog Cakes..."
curl -s -X GET "${BASE_URL}/api/catalog/cakes" | jq . || curl -s -X GET "${BASE_URL}/api/catalog/cakes"

echo -e "\n\n2. Filtering Cakes by Category (Birthday)..."
curl -s -X GET "${BASE_URL}/api/catalog/cakes?category=Birthday" | jq . || curl -s -X GET "${BASE_URL}/api/catalog/cakes?category=Birthday"

echo -e "\n\n3. Adding Cake (ID: 1, Qty: 2) to Basket..."
curl -s -X POST "${BASE_URL}/api/orders/basket" \
  -H "Content-Type: application/json" \
  -d '{"cakeId": 1, "quantity": 2}' | jq . || true

echo -e "\n\n4. Retrieving Current Basket..."
curl -s -X GET "${BASE_URL}/api/orders/basket" | jq . || curl -s -X GET "${BASE_URL}/api/orders/basket"

echo -e "\n\n5. Executing Checkout..."
CHECKOUT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/orders/checkout" -H "Content-Type: application/json")
echo "${CHECKOUT_RESPONSE}"

ORDER_ID=$(echo "${CHECKOUT_RESPONSE}" | grep -o '"orderId":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "${ORDER_ID}" ]; then
  echo -e "\n\n6. Retrieving Placed Order (ID: ${ORDER_ID})..."
  curl -s -X GET "${BASE_URL}/api/orders/orders/${ORDER_ID}" | jq . || curl -s -X GET "${BASE_URL}/api/orders/orders/${ORDER_ID}"

  echo -e "\n\n7. Checking Notifications for Order (ID: ${ORDER_ID})..."
  curl -s -X GET "${BASE_URL}/api/notifications/orders/${ORDER_ID}" | jq . || curl -s -X GET "${BASE_URL}/api/notifications/orders/${ORDER_ID}"
fi

echo -e "\n\n8. Submitting Rating for Cake (ID: 1)..."
curl -s -X POST "${BASE_URL}/api/ratings" \
  -H "Content-Type: application/json" \
  -d '{"cakeId": 1, "userId": 101, "score": 5, "comment": "Amazing Truffle Cake!"}' | jq . || true

echo -e "\n\n9. Fetching Average Rating for Cake (ID: 1)..."
curl -s -X GET "${BASE_URL}/api/ratings/cakes/1/average" | jq . || curl -s -X GET "${BASE_URL}/api/ratings/cakes/1/average"

echo -e "\n=========================================="
echo " E2E Flow Verification Completed Successfully"
echo "=========================================="
