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
| **Exchange Name** | `order.events.exchange` |
| **Exchange Type** | Topic Exchange (`TopicExchange`) |
| **Routing Key** | `order.completed` |
| **Queue Name** | `order.completed.queue` |
| **Durability** | Durable |

---

## 3. JSON Payload Schema

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "orderId": 1,
  "orderDate": "2026-08-10T13:53:31.366033661",
  "totalAmount": 1598.0,
  "status": "CREATED"
}
```

### Field Definitions

| Field Name | Type | Description |
| :--- | :--- | :--- |
| `eventId` | `String` (UUID) | Unique message identifier generated at broadcast time to support consumer-side idempotency. |
| `orderId` | `Long` | Primary key identifier of the placed order in `cake_order.orders`. |
| `orderDate` | `LocalDateTime` / ISO-8601 | Timestamp indicating when the order checkout occurred. |
| `totalAmount` | `Double` | Total purchase amount for the checkout in standard currency units. |
| `status` | `String` | Order processing status (e.g. `CREATED`). |

---

## 4. Lifecycle & Processing Workflow

### A. Producer Trigger (`order-service`)
1. User invokes `POST /api/orders/checkout`.
2. `order-service` creates an `Order` entity, calculates total cost from `BasketItem` entities, saves `orders` & `order_items` in MySQL database `cake_order`, and flushes the basket.
3. Upon database commit, `order-service` instantiates `OrderCompletedEvent` with a new `UUID.randomUUID()`.
4. `RabbitTemplate` serializes the payload to JSON and sends it to `order.events.exchange` with routing key `order.completed`.

### B. Consumer Processing (`notification-service`)
1. `OrderCompletedListener` receives the payload from `order.completed.queue`.
2. `NotificationServiceImpl` checks `findByEventId(event.getEventId())`. If an existing record with status `SENT` is found, it returns the existing response immediately to maintain idempotency without re-sending the email.
3. If an existing record has status `FAILED` or `PENDING` (from a previous failed attempt), or if it is a new event, `NotificationServiceImpl` sets/resets the status to `PENDING` and attempts email delivery.
4. `EmailNotificationSender` formats an HTML order confirmation email containing the order details and sends it via SMTP to MailHog (`localhost:1025`).
5. On successful delivery, the notification status in MySQL is updated to `SENT`. If an exception occurs, status is set to `FAILED` and the exception is re-thrown for Spring AMQP retry handling.