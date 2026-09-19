$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$BaseUrl = $env:BASE_URL
if ([string]::IsNullOrWhiteSpace($BaseUrl)) { $BaseUrl = 'http://localhost:8080' }
$AdminUsername = $env:ADMIN_USERNAME
if ([string]::IsNullOrWhiteSpace($AdminUsername)) { $AdminUsername = 'admin' }
$AdminPassword = $env:ADMIN_PASSWORD
if ([string]::IsNullOrWhiteSpace($AdminPassword)) { $AdminPassword = 'Admin@12345' }

$TestUsername = 'cdtest_' + (Get-Date -Format 'yyyyMMddHHmmss') + '_' + (Get-Random -Maximum 99999)
$TestEmail = "$TestUsername@example.com"
$TestPassword = 'TestPass1234A'

$PassCount = 0
$FailCount = 0

function Write-Pass($message) {
    Write-Host "  [PASS] $message" -ForegroundColor Green
    $script:PassCount++
}

function Write-Fail($message) {
    Write-Host "  [FAIL] $message" -ForegroundColor Red
    $script:FailCount++
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [string]$Token = $null
    )
    $uri = "$BaseUrl$Path"
    $headers = @{}
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }

    $params = @{
        Uri             = $uri
        Method          = $Method
        Headers         = $headers
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $params['Body'] = ($Body | ConvertTo-Json -Depth 5)
        $params['ContentType'] = 'application/json'
    }

    $status = 0
    $content = ''
    try {
        $response = Invoke-WebRequest @params
        $status = [int]$response.StatusCode
        $content = $response.Content
    }
    catch {
        $webResponse = $_.Exception.Response
        if ($webResponse) {
            $status = [int]$webResponse.StatusCode.value__
            try {
                $stream = $webResponse.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $content = $reader.ReadToEnd()
            }
            catch { $content = '' }
        }
        else {
            $status = 0
            $content = $_.Exception.Message
        }
    }

    $json = $null
    if ($content) {
        try { $json = $content | ConvertFrom-Json } catch { $json = $null }
    }

    return [pscustomobject]@{ Status = $status; Content = $content; Json = $json }
}

Write-Host "=========================================="
Write-Host " Cake Delight Authenticated E2E Verification"
Write-Host " Target API Gateway: $BaseUrl"
Write-Host " Test user: $TestUsername"
Write-Host "=========================================="

Write-Host "`n1. Registering unique test user..."
$r = Invoke-Api -Method POST -Path '/api/auth/register' -Body @{ username = $TestUsername; email = $TestEmail; password = $TestPassword }
if ($r.Status -eq 201) { Write-Pass "Register test user (201)" } else { Write-Fail "Register test user (expected 201, got $($r.Status))" }

Write-Host "`n2. Logging in as test user..."
$r = Invoke-Api -Method POST -Path '/api/auth/login' -Body @{ username = $TestUsername; password = $TestPassword }
if ($r.Status -eq 200) { Write-Pass "Login test user (200)" } else { Write-Fail "Login test user (expected 200, got $($r.Status))" }
$TestToken = $null; $TestUserId = $null
if ($r.Json) { $TestToken = $r.Json.token; $TestUserId = $r.Json.id }
if ($TestToken) { Write-Pass "JWT extracted from login response" } else { Write-Fail "JWT extraction from login response" }

Write-Host "`n3. Fetching Catalog Cakes..."
$r = Invoke-Api -Method GET -Path '/api/catalog/cakes'
if ($r.Status -eq 200) { Write-Pass "GET catalog cakes (200)" } else { Write-Fail "GET catalog cakes (expected 200, got $($r.Status))" }
$CakeId = 1
if ($r.Json -and $r.Json.Count -gt 0) { $CakeId = $r.Json[0].id }

Write-Host "`n4. Filtering Cakes by Category (Birthday)..."
$r = Invoke-Api -Method GET -Path '/api/catalog/cakes?category=Birthday'
if ($r.Status -eq 200) { Write-Pass "GET catalog cakes filtered by category (200)" } else { Write-Fail "GET catalog cakes filtered (expected 200, got $($r.Status))" }

Write-Host "`n5. Adding Cake (ID: $CakeId, Qty: 2) to Basket (authenticated)..."
$r = Invoke-Api -Method POST -Path '/api/orders/basket' -Body @{ cakeId = $CakeId; quantity = 2 } -Token $TestToken
if ($r.Status -eq 201) { Write-Pass "POST basket add (201)" } else { Write-Fail "POST basket add (expected 201, got $($r.Status))" }

Write-Host "`n6. Retrieving Current Basket (authenticated)..."
$r = Invoke-Api -Method GET -Path '/api/orders/basket' -Token $TestToken
if ($r.Status -eq 200) { Write-Pass "GET basket (200)" } else { Write-Fail "GET basket (expected 200, got $($r.Status))" }

Write-Host "`n7. Executing Checkout (authenticated)..."
$r = Invoke-Api -Method POST -Path '/api/orders/checkout' -Token $TestToken
if ($r.Status -eq 201) { Write-Pass "POST checkout (201)" } else { Write-Fail "POST checkout (expected 201, got $($r.Status))" }
$OrderId = $null
if ($r.Json -and $r.Json.order) { $OrderId = $r.Json.order.orderId }

Write-Host "`n8. Verifying basket is empty after checkout..."
$r = Invoke-Api -Method GET -Path '/api/orders/basket' -Token $TestToken
$itemsEmpty = $true
if ($r.Json -and $r.Json.items -and $r.Json.items.Count -gt 0) { $itemsEmpty = $false }
if ($r.Status -eq 200 -and $itemsEmpty) { Write-Pass "Basket is empty immediately after checkout" } else { Write-Fail "Basket still contains items after checkout" }

Write-Host "`n9. Retrying checkout with empty basket (must be rejected)..."
$r = Invoke-Api -Method POST -Path '/api/orders/checkout' -Token $TestToken
if ($r.Status -ne 200 -and $r.Status -ne 201) { Write-Pass "Second checkout with empty basket rejected (HTTP $($r.Status))" } else { Write-Fail "Second checkout with empty basket unexpectedly succeeded (HTTP $($r.Status))" }

Write-Host "`n10. Retrieving order history (authenticated)..."
$r = Invoke-Api -Method GET -Path '/api/orders' -Token $TestToken
$hasOrder = $false
if ($r.Json -and $OrderId) { $hasOrder = [bool]($r.Json | Where-Object { $_.orderId -eq $OrderId }) }
if ($r.Status -eq 200 -and $hasOrder) { Write-Pass "Order history contains the newly created order" } else { Write-Fail "Order history missing the newly created order (HTTP $($r.Status))" }

Write-Host "`n11. Submitting Rating for Cake (ID: $CakeId) as authenticated user..."
$r = Invoke-Api -Method POST -Path '/api/ratings' -Body @{ cakeId = $CakeId; userId = $TestUserId; score = 5; comment = 'Automated test rating' } -Token $TestToken
if ($r.Status -eq 201) { Write-Pass "POST rating submission (201)" } else { Write-Fail "POST rating submission (expected 201, got $($r.Status))" }

Write-Host "`n12. Fetching Ratings for Cake (ID: $CakeId)..."
$r = Invoke-Api -Method GET -Path "/api/ratings/cakes/$CakeId"
$hasUsername = $false
if ($r.Json) { $hasUsername = [bool]($r.Json | Where-Object { $_.PSObject.Properties.Name -contains 'username' -and $_.username }) }
if ($r.Status -eq 200 -and $hasUsername) { Write-Pass "GET ratings list (200) includes username field" } else { Write-Fail "GET ratings list missing username field or wrong status (HTTP $($r.Status))" }

Write-Host "`n13. Verifying protected endpoint rejects missing Authorization..."
$r = Invoke-Api -Method GET -Path '/api/orders'
if ($r.Status -eq 401) { Write-Pass "GET /api/orders without token returns 401" } else { Write-Fail "Expected 401 without token, got $($r.Status)" }

Write-Host "`n14. Verifying normal user cannot perform admin cake creation (RBAC)..."
$r = Invoke-Api -Method POST -Path '/api/catalog/cakes' -Body @{ name = 'RBAC Test Cake'; category = 'Test'; price = 1; stock = 1; description = 'rbac test'; imageUrl = '' } -Token $TestToken
if ($r.Status -eq 403) { Write-Pass "Normal user admin cake creation rejected (403)" } else { Write-Fail "Expected 403 for normal user admin action, got $($r.Status)" }

Write-Host "`n15. Logging in as admin ($AdminUsername)..."
$r = Invoke-Api -Method POST -Path '/api/auth/login' -Body @{ username = $AdminUsername; password = $AdminPassword }
$AdminToken = $null; $AdminRole = $null
if ($r.Json) { $AdminToken = $r.Json.token; $AdminRole = $r.Json.role }
if ($r.Status -eq 200 -and $AdminRole -eq 'ROLE_ADMIN') { Write-Pass "Admin login succeeded with ROLE_ADMIN" } else { Write-Fail "Admin login failed or role was not ROLE_ADMIN (HTTP $($r.Status), role=$AdminRole)" }

$TempCakeId = $null
if ($AdminToken) {
    Write-Host "`n16. Creating a temporary cake as admin..."
    $r = Invoke-Api -Method POST -Path '/api/catalog/cakes' -Body @{ name = 'Automated Test Cake'; category = 'Test'; price = 1; stock = 1; description = 'temporary test cake'; imageUrl = '' } -Token $AdminToken
    if ($r.Status -eq 201) { Write-Pass "Admin cake creation (201)" } else { Write-Fail "Admin cake creation (expected 201, got $($r.Status))" }
    if ($r.Json) { $TempCakeId = $r.Json.id }

    if ($TempCakeId) {
        Write-Host "`n17. Updating the temporary cake as admin..."
        $r = Invoke-Api -Method PUT -Path "/api/catalog/cakes/$TempCakeId" -Body @{ name = 'Automated Test Cake Updated'; category = 'Test'; price = 2; stock = 2; description = 'updated'; imageUrl = '' } -Token $AdminToken
        if ($r.Status -eq 200) { Write-Pass "Admin cake update (200)" } else { Write-Fail "Admin cake update (expected 200, got $($r.Status))" }

        Write-Host "`n18. Deleting the temporary cake as admin (cleanup)..."
        $r = Invoke-Api -Method DELETE -Path "/api/catalog/cakes/$TempCakeId" -Token $AdminToken
        if ($r.Status -eq 200 -or $r.Status -eq 204) { Write-Pass "Admin cake delete/cleanup ($($r.Status))" } else { Write-Fail "Admin cake delete/cleanup (expected 200/204, got $($r.Status))" }
    }
    else {
        Write-Fail "Could not determine temporary cake id for update/delete"
    }
}
else {
    Write-Fail "Skipped admin CRUD steps because admin login did not return a token"
}

if ($OrderId) {
    Write-Host "`n19. Checking notification/order-completion records for Order (ID: $OrderId)..."
    $r = Invoke-Api -Method GET -Path "/api/notifications/$OrderId" -Token $TestToken
    if ($r.Status -eq 200) { Write-Pass "GET notifications for order (200)" } else { Write-Fail "GET notifications for order (expected 200, got $($r.Status))" }
}
else {
    Write-Fail "Skipped notification check because no order id was captured"
}

Write-Host "`n=========================================="
Write-Host " Results: $PassCount passed, $FailCount failed"
Write-Host "=========================================="

if ($FailCount -gt 0) { exit 1 } else { exit 0 }
