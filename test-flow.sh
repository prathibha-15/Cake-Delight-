#!/usr/bin/env bash
# Cake Delight End-to-End Authenticated API Verification Script
#
# Verifies the CURRENT authenticated application flow:
# register -> login -> JWT Bearer usage -> catalog -> basket -> checkout ->
# order history -> ratings -> 401/403 RBAC -> admin CRUD -> notifications.
#
# The gateway derives trusted user identity (X-User-Id/X-User-Role/X-User-Name)
# from the JWT itself; this script never sends those headers directly.

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin@12345}"

TEST_USERNAME="cdtest_$(date +%s)_$$"
TEST_EMAIL="${TEST_USERNAME}@example.com"
TEST_PASSWORD="TestPass1234A"

PASS_COUNT=0
FAIL_COUNT=0

pass() { echo "  [PASS] $1"; PASS_COUNT=$((PASS_COUNT + 1)); }
fail() { echo "  [FAIL] $1"; FAIL_COUNT=$((FAIL_COUNT + 1)); }

# Extracts a top-level JSON string field's value without requiring jq.
json_string_field() {
  echo "$1" | grep -o "\"$2\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed -E 's/.*:[[:space:]]*"([^"]*)"/\1/'
}

# Extracts a top-level JSON numeric field's value without requiring jq.
json_number_field() {
  echo "$1" | grep -o "\"$2\"[[:space:]]*:[[:space:]]*[0-9][0-9]*" | head -1 | sed -E 's/.*:[[:space:]]*([0-9]+)/\1/'
}

# Performs an HTTP request and sets HTTP_STATUS and RESPONSE_BODY.
# Usage: http_call METHOD PATH [BODY] [BEARER_TOKEN]
http_call() {
  local method="$1" path="$2" body="${3:-}" token="${4:-}"
  local -a curl_args=(-s -o /tmp/cd_flow_resp.$$ -w "%{http_code}" -X "$method" "${BASE_URL}${path}")
  if [ -n "$token" ]; then
    curl_args+=(-H "Authorization: Bearer ${token}")
  fi
  if [ -n "$body" ]; then
    curl_args+=(-H "Content-Type: application/json" -d "$body")
  fi
  HTTP_STATUS=$(curl "${curl_args[@]}")
  RESPONSE_BODY=$(cat /tmp/cd_flow_resp.$$ 2>/dev/null)
  rm -f /tmp/cd_flow_resp.$$
}

echo "=========================================="
echo " Cake Delight Authenticated E2E Verification"
echo " Target API Gateway: ${BASE_URL}"
echo " Test user: ${TEST_USERNAME}"
echo "=========================================="

echo -e "\n1. Registering unique test user..."
http_call POST "/api/auth/register" "{\"username\":\"${TEST_USERNAME}\",\"email\":\"${TEST_EMAIL}\",\"password\":\"${TEST_PASSWORD}\"}"
[ "$HTTP_STATUS" = "201" ] && pass "Register test user (201)" || fail "Register test user (expected 201, got ${HTTP_STATUS})"

echo -e "\n2. Logging in as test user..."
http_call POST "/api/auth/login" "{\"username\":\"${TEST_USERNAME}\",\"password\":\"${TEST_PASSWORD}\"}"
if [ "$HTTP_STATUS" = "200" ]; then
  pass "Login test user (200)"
else
  fail "Login test user (expected 200, got ${HTTP_STATUS})"
fi
TEST_TOKEN=$(json_string_field "$RESPONSE_BODY" "token")
TEST_USER_ID=$(json_number_field "$RESPONSE_BODY" "id")
if [ -n "$TEST_TOKEN" ]; then
  pass "JWT extracted from login response"
else
  fail "JWT extraction from login response"
fi

echo -e "\n3. Fetching Catalog Cakes..."
http_call GET "/api/catalog/cakes"
[ "$HTTP_STATUS" = "200" ] && pass "GET catalog cakes (200)" || fail "GET catalog cakes (expected 200, got ${HTTP_STATUS})"
CAKE_ID=$(json_number_field "$RESPONSE_BODY" "id")
if [ -z "$CAKE_ID" ]; then
  echo "  No cake id found in catalog response; defaulting to 1."
  CAKE_ID=1
fi

echo -e "\n4. Filtering Cakes by Category (Birthday)..."
http_call GET "/api/catalog/cakes?category=Birthday"
[ "$HTTP_STATUS" = "200" ] && pass "GET catalog cakes filtered by category (200)" || fail "GET catalog cakes filtered (expected 200, got ${HTTP_STATUS})"

echo -e "\n5. Adding Cake (ID: ${CAKE_ID}, Qty: 2) to Basket (authenticated)..."
http_call POST "/api/orders/basket" "{\"cakeId\": ${CAKE_ID}, \"quantity\": 2}" "$TEST_TOKEN"
[ "$HTTP_STATUS" = "201" ] && pass "POST basket add (201)" || fail "POST basket add (expected 201, got ${HTTP_STATUS})"

echo -e "\n6. Retrieving Current Basket (authenticated)..."
http_call GET "/api/orders/basket" "" "$TEST_TOKEN"
[ "$HTTP_STATUS" = "200" ] && pass "GET basket (200)" || fail "GET basket (expected 200, got ${HTTP_STATUS})"

echo -e "\n7. Executing Checkout (authenticated)..."
http_call POST "/api/orders/checkout" "" "$TEST_TOKEN"
[ "$HTTP_STATUS" = "201" ] && pass "POST checkout (201)" || fail "POST checkout (expected 201, got ${HTTP_STATUS})"
ORDER_ID=$(json_number_field "$RESPONSE_BODY" "orderId")

echo -e "\n8. Verifying basket is empty after checkout..."
http_call GET "/api/orders/basket" "" "$TEST_TOKEN"
if [ "$HTTP_STATUS" = "200" ] && ! echo "$RESPONSE_BODY" | grep -q "\"cakeId\""; then
  pass "Basket is empty immediately after checkout"
else
  fail "Basket still contains items after checkout"
fi

echo -e "\n9. Retrying checkout with empty basket (must be rejected)..."
http_call POST "/api/orders/checkout" "" "$TEST_TOKEN"
if [ "$HTTP_STATUS" != "200" ] && [ "$HTTP_STATUS" != "201" ]; then
  pass "Second checkout with empty basket rejected (HTTP ${HTTP_STATUS})"
else
  fail "Second checkout with empty basket unexpectedly succeeded (HTTP ${HTTP_STATUS})"
fi

echo -e "\n10. Retrieving order history (authenticated)..."
http_call GET "/api/orders" "" "$TEST_TOKEN"
if [ "$HTTP_STATUS" = "200" ] && [ -n "$ORDER_ID" ] && echo "$RESPONSE_BODY" | grep -q "\"orderId\":${ORDER_ID}"; then
  pass "Order history contains the newly created order"
else
  fail "Order history missing the newly created order (HTTP ${HTTP_STATUS})"
fi

echo -e "\n11. Submitting Rating for Cake (ID: ${CAKE_ID}) as authenticated user..."
http_call POST "/api/ratings" "{\"cakeId\": ${CAKE_ID}, \"userId\": ${TEST_USER_ID}, \"score\": 5, \"comment\": \"Automated test rating\"}" "$TEST_TOKEN"
[ "$HTTP_STATUS" = "201" ] && pass "POST rating submission (201)" || fail "POST rating submission (expected 201, got ${HTTP_STATUS})"

echo -e "\n12. Fetching Ratings for Cake (ID: ${CAKE_ID})..."
http_call GET "/api/ratings/cakes/${CAKE_ID}"
if [ "$HTTP_STATUS" = "200" ] && echo "$RESPONSE_BODY" | grep -q "\"username\""; then
  pass "GET ratings list (200) includes username field"
else
  fail "GET ratings list missing username field or wrong status (HTTP ${HTTP_STATUS})"
fi

echo -e "\n13. Verifying protected endpoint rejects missing Authorization..."
http_call GET "/api/orders"
[ "$HTTP_STATUS" = "401" ] && pass "GET /api/orders without token returns 401" || fail "Expected 401 without token, got ${HTTP_STATUS}"

echo -e "\n14. Verifying normal user cannot perform admin cake creation (RBAC)..."
http_call POST "/api/catalog/cakes" "{\"name\":\"RBAC Test Cake\",\"category\":\"Test\",\"price\":1,\"stock\":1,\"description\":\"rbac test\",\"imageUrl\":\"\"}" "$TEST_TOKEN"
[ "$HTTP_STATUS" = "403" ] && pass "Normal user admin cake creation rejected (403)" || fail "Expected 403 for normal user admin action, got ${HTTP_STATUS}"

echo -e "\n15. Logging in as admin (${ADMIN_USERNAME})..."
http_call POST "/api/auth/login" "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}"
ADMIN_TOKEN=$(json_string_field "$RESPONSE_BODY" "token")
ADMIN_ROLE=$(json_string_field "$RESPONSE_BODY" "role")
if [ "$HTTP_STATUS" = "200" ] && [ "$ADMIN_ROLE" = "ROLE_ADMIN" ]; then
  pass "Admin login succeeded with ROLE_ADMIN"
else
  fail "Admin login failed or role was not ROLE_ADMIN (HTTP ${HTTP_STATUS}, role=${ADMIN_ROLE})"
fi

TEMP_CAKE_ID=""
if [ -n "$ADMIN_TOKEN" ]; then
  echo -e "\n16. Creating a temporary cake as admin..."
  http_call POST "/api/catalog/cakes" "{\"name\":\"Automated Test Cake\",\"category\":\"Test\",\"price\":1,\"stock\":1,\"description\":\"temporary test cake\",\"imageUrl\":\"\"}" "$ADMIN_TOKEN"
  [ "$HTTP_STATUS" = "201" ] && pass "Admin cake creation (201)" || fail "Admin cake creation (expected 201, got ${HTTP_STATUS})"
  TEMP_CAKE_ID=$(json_number_field "$RESPONSE_BODY" "id")

  if [ -n "$TEMP_CAKE_ID" ]; then
    echo -e "\n17. Updating the temporary cake as admin..."
    http_call PUT "/api/catalog/cakes/${TEMP_CAKE_ID}" "{\"name\":\"Automated Test Cake Updated\",\"category\":\"Test\",\"price\":2,\"stock\":2,\"description\":\"updated\",\"imageUrl\":\"\"}" "$ADMIN_TOKEN"
    [ "$HTTP_STATUS" = "200" ] && pass "Admin cake update (200)" || fail "Admin cake update (expected 200, got ${HTTP_STATUS})"

    echo -e "\n18. Deleting the temporary cake as admin (cleanup)..."
    http_call DELETE "/api/catalog/cakes/${TEMP_CAKE_ID}" "" "$ADMIN_TOKEN"
    if [ "$HTTP_STATUS" = "200" ] || [ "$HTTP_STATUS" = "204" ]; then
      pass "Admin cake delete/cleanup (${HTTP_STATUS})"
    else
      fail "Admin cake delete/cleanup (expected 200/204, got ${HTTP_STATUS})"
    fi
  else
    fail "Could not determine temporary cake id for update/delete"
  fi
else
  fail "Skipped admin CRUD steps because admin login did not return a token"
fi

if [ -n "$ORDER_ID" ]; then
  echo -e "\n19. Checking notification/order-completion records for Order (ID: ${ORDER_ID})..."
  http_call GET "/api/notifications/${ORDER_ID}" "" "$TEST_TOKEN"
  [ "$HTTP_STATUS" = "200" ] && pass "GET notifications for order (200)" || fail "GET notifications for order (expected 200, got ${HTTP_STATUS})"
else
  fail "Skipped notification check because no order id was captured"
fi

echo -e "\n=========================================="
echo " Results: ${PASS_COUNT} passed, ${FAIL_COUNT} failed"
echo "=========================================="

if [ "$FAIL_COUNT" -gt 0 ]; then
  exit 1
fi
exit 0
