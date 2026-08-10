@echo off
set BASE_URL=http://localhost:8080

echo ==========================================
echo  Cake Delight E2E Flow Verification
echo  Target API Gateway: %BASE_URL%
echo ==========================================

echo.
echo 1. Fetching Catalog Cakes...
curl -s -X GET %BASE_URL%/api/catalog/cakes

echo.
echo.
echo 2. Filtering Cakes by Category (Birthday)...
curl -s -X GET "%BASE_URL%/api/catalog/cakes?category=Birthday"

echo.
echo.
echo 3. Adding Cake (ID: 1, Qty: 2) to Basket...
curl -s -X POST %BASE_URL%/api/orders/basket -H "Content-Type: application/json" -d "{\"cakeId\": 1, \"quantity\": 2}"

echo.
echo.
echo 4. Retrieving Current Basket...
curl -s -X GET %BASE_URL%/api/orders/basket

echo.
echo.
echo 5. Executing Checkout...
curl -s -X POST %BASE_URL%/api/orders/checkout -H "Content-Type: application/json"

echo.
echo.
echo 6. Submitting Rating for Cake (ID: 1)...
curl -s -X POST %BASE_URL%/api/ratings -H "Content-Type: application/json" -d "{\"cakeId\": 1, \"userId\": 101, \"score\": 5, \"comment\": \"Amazing Truffle Cake!\"}"

echo.
echo.
echo 7. Fetching Average Rating for Cake (ID: 1)...
curl -s -X GET %BASE_URL%/api/ratings/cakes/1/average

echo.
echo ==========================================
echo  E2E Flow Verification Completed
echo ==========================================
