$services = @("api-gateway", "catalog-service", "order-service", "rating-service", "notification-service", "user-service")

foreach ($svc in $services) {
    Write-Host "Building $svc..."
    Push-Location "c:\Users\prath\Desktop\cake-delight\$svc"
    mvn -q -DskipTests package
    Pop-Location
}
Write-Host "All JARs built successfully!"
