# 📩 Order Completed Event Contract & Schema Specification

This document details the exact AMQP messaging event contract implemented in **Cake Delight** for asynchronous event propagation between `order-service` and `notification-service`.

---

## 1. Event Overview

- **Event Name**: `OrderCompletedEvent`
- **Producer**: `order-service` (`com.cakedelight.order.service.OrderServiceImpl`)
- **Consumer**: `notification-service` (`com.cakedelight.notification.listener.OrderCompletedListener`)
- **Messaging Protocol**: AMQP 0-9-1 over RabbitMQ
- **Payload Format**: JSON (`application/json`)

---

## 2. RabbitMQ Routing Specification

| Attribute | Configured Value |
| :--- | :--- |
| **Exchange Name** | `cake-delight.exchange` |
| **Exchange Type** | Direct Exchange (`DirectExchange`) |
| **Routing Key** | `order.completed` |
| **Queue Name** | `notification.order.completed` |
| **Durability** | Durable |
| **Dead Letter Exchange (DLX)** | `cake-delight.dlx` |
| **Dead Letter Queue (DLQ)** | `notification.order.completed.dlq` |
| **Dead Letter Routing Key** | `notification.order.completed.dlq` |

---

## 3. JSON Payload Schema

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "orderId": 1,
  "userId": 12,
  "orderDate": "2026-09-12T10:57:23.393151",
  "totalAmount": 1598.0,
  "status": "CREATED"
}
```

### Field Definitions

| Field Name | Type | Description |
| :--- | :--- | :--- |
| `eventId` | `UUID` (String) | Unique message identifier generated at broadcast time to support consumer-side idempotency. |
| `orderId` | `Long` | Primary key identifier of the placed order in `cake_order.orders`. |
| `userId` | `Long` | Foreign key identifier of the purchasing user in `user_db.users`. |
| `orderDate` | `LocalDateTime` / ISO-8601 | Timestamp indicating when the order checkout occurred. |
| `totalAmount` | `Double` | Total purchase amount for the checkout in standard currency units. |
| `status` | `String` | Order processing status (e.g. `CREATED`). |

---

## 4. Lifecycle & Processing Workflow

### A. Producer Trigger (`order-service`)
1. User invokes `POST /api/orders/checkout`.
2. `order-service` creates an `Order` entity, calculates total cost from `BasketItem` entities, saves `orders` and `order_items` in MySQL database `cake_order`, and creates an `OrderCompletedEvent`.
3. The event contains a new `UUID.randomUUID()` and the authenticated user's ID. The publisher attempts delivery up to three times with a 500 ms delay between attempts.
4. `RabbitTemplate` serializes the payload with `Jackson2JsonMessageConverter` and sends it to `cake-delight.exchange` with routing key `order.completed`. The basket is deleted after the publish call; a final publish failure is logged by the publisher rather than thrown to the checkout controller.

### B. Consumer Processing & Idempotency (`notification-service`)
1. `OrderCompletedListener` receives the payload from queue `notification.order.completed`.
2. `NotificationServiceImpl` checks `findByEventId(event.getEventId())`. If an existing record with status `SENT` is found, it returns immediately to maintain consumer idempotency without re-sending the email.
3. If an existing record has status `FAILED` or `PENDING` (from a previous attempt), or if it is a new event, `NotificationServiceImpl` records/resets the status to `PENDING` and attempts email delivery.
4. `EmailNotificationSender` formats a plain-text order confirmation email containing the order details and sends it via SMTP to MailHog (`localhost:1025`).
5. On successful delivery, the notification status in MySQL is updated to `SENT`.
6. If SMTP fails, the notification service records status `FAILED` in `notification_db`. Listener retry is configured for three attempts with a 1 s initial interval, 2.0 multiplier, and 5 s maximum interval. The listener catches processing exceptions; only rejected or otherwise unhandled failures can reach the dead-letter exchange and `notification.order.completed.dlq`.