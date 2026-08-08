# Order Completed Event Contract

## Event name

`OrderCompletedEvent`

## Routing

- Exchange: `order.events.exchange`
- Queue: `order.completed.queue`
- Routing key: `order.completed`

## JSON payload

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "orderId": 101,
  "orderDate": "2026-08-08T12:30:00",
  "totalAmount": 499.0,
  "status": "CREATED"
}
```

## Field meanings

- `eventId`: Stable UUID for idempotency and duplicate detection.
- `orderId`: Order identifier produced by order-service.
- `orderDate`: Timestamp of the completed order in ISO-8601 local date-time format.
- `totalAmount`: Final order amount.
- `status`: Order status string emitted by order-service.

## Consumer expectations

- Notification Service must ignore duplicate `eventId` values.
- Notification Service persists one notification row per unique event.
- The consumer treats the payload as the source of truth for the email body.

## Compatibility notes

- `eventId` must stay present for idempotency.
- `status` should remain a stable string value.
- If the payload grows later, new fields should be added without breaking existing consumers.

## Local notification delivery

The notification branch uses Spring Mail and a local SMTP sink during development so the send path is still exercised without requiring a production mailbox. Swap the `SPRING_MAIL_*` environment variables to Mailtrap, Gmail app password, or another SMTP provider when you want external delivery.