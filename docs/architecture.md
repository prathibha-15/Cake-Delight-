# Cake Delight - Microservices Architecture Documentation

## 1. System Overview

**Cake Delight** is a production-ready, cloud-native microservices application built with **Java 17**, **Spring Boot 3**, **Spring Cloud Gateway**, **MySQL 8.0**, **RabbitMQ**, and **MailHog**, containerized with **Docker** and orchestrated using **Kubernetes**.

The platform provides a complete e-commerce flow: browsing cake catalog, adding items to basket, placing orders, asynchronous notification sending via AMQP event messaging, and rating purchased cakes.

---

## 2. Microservice Component Breakdown

```
[ Client / Browser ]
        │
        ▼ (HTTP :8080)
┌─────────────────────────────────────────────────────────────────┐
│                           API Gateway                           │
│                     (Spring Cloud Gateway)                      │
└──────┬──────────────┬──────────────────┬─────────────────┬──────┘
       │              │                  │                 │
       ▼              ▼                  ▼                 ▼
┌──────────────┐┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Catalog       ││Order         │  │Rating        │  │Notification  │
│Service       ││Service       │  │Service       │  │Service       │
│(:8081)       ││(:8082)       │  │(:8083)       │  │(:8084)       │
└──────┬───────┘└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │               │                 │                 │
       │               ├─────────────────┼─────────────────┤
       ▼               ▼                 ▼                 ▼
 ┌──────────────────────────────────────────────────────────────┐
 │                      MySQL Database                          │
 │ (Databases: cake_catalog, cake_order, cake_rating,           │
 │  notification_db)                                            │
 └──────────────────────────────────────────────────────────────┘
                       │
                       │ (OrderCompletedEvent)
                       ▼
 ┌──────────────────────────────────────────────────────────────┐
 │                     RabbitMQ Broker                          │
 │  Exchange: order.events.exchange                             │
 │  Queue: order.completed.queue                                │
 └─────────────────────────────┬────────────────────────────────┘
                               │
                               ▼
 ┌──────────────────────────────────────────────────────────────┐
 │                     MailHog SMTP Sink                        │
 │  Port: 1025 (SMTP), Port: 8025 (Web UI)                      │
 └──────────────────────────────────────────────────────────────┘
```

### Services Description

1. **API Gateway (`api-gateway` - Port 8080)**
   - Single entry point for external clients.
   - Routes requests to `/api/catalog/**`, `/api/orders/**`, `/api/ratings/**`, and `/api/notifications/**`.
   - Serves the frontend web UI storefront static assets at `/`.

2. **Catalog Service (`catalog-service` - Port 8081)**
   - Manages cake items, prices, categories, stock, and image URLs.
   - Database: `cake_catalog`.
   - Provides filtering by category, cake name search, and price range.

3. **Order Service (`order-service` - Port 8082)**
   - Manages shopping basket (`/api/orders/basket`) and checkout execution (`/api/orders/checkout`).
   - Database: `cake_order`.
   - Communicates synchronously with `catalog-service` via REST to validate cake details.
   - Publishes an `OrderCompletedEvent` to RabbitMQ upon successful checkout.

4. **Rating Service (`rating-service` - Port 8083)**
   - Manages customer ratings and reviews for cakes.
   - Database: `cake_rating`.
   - Computes average rating scores and review counts.

5. **Notification Service (`notification-service` - Port 8084)**
   - Asynchronously consumes `OrderCompletedEvent` from RabbitMQ `order.completed.queue`.
   - Database: `notification_db`.
   - Guarantees idempotent event processing using `eventId`.
   - Sends notification emails to the MailHog SMTP server.

---

## 3. Database Architecture

MySQL 8.0 serves as the primary relational database with persistent storage (`mysql-data` volume / PVC):

- `cake_catalog`: Contains the `cakes` table storing item definitions.
- `cake_order`: Contains `orders`, `order_items`, and `basket_items` tables.
- `cake_rating`: Contains the `ratings` table storing cake scores and customer comments.
- `notification_db`: Contains the `notifications` table tracking event processing status (`PENDING`, `SENT`, `FAILED`).

---

## 4. Asynchronous Messaging Contract

- **Exchange**: `order.events.exchange` (Topic / Direct)
- **Queue**: `order.completed.queue`
- **Routing Key**: `order.completed`
- **Payload (`OrderCompletedEvent`)**:
  ```json
  {
    "eventId": "550e8400-e29b-41d4-a716-446655440000",
    "orderId": 101,
    "orderDate": "2026-08-10T10:00:00",
    "totalAmount": 1598.0,
    "status": "CREATED"
  }
  ```

---

## 5. Deployment & Kubernetes Architecture

The platform supports both local development and Kubernetes deployment:

- **Local / Docker Compose**: Defined in `docker-compose.yml` linking MySQL, RabbitMQ, MailHog, and all 5 microservices.
- **Kubernetes Manifests (`k8s/`)**:
  - `mysql-deployment.yaml` & `mysql-pvc.yaml` (Persistent MySQL deployment with PVC)
  - `rabbitmq-deployment.yaml` & `mailhog-deployment.yaml`
  - `catalog-service-deployment.yaml`, `order-service-deployment.yaml`, `rating-service-deployment.yaml`, `notification-service-deployment.yaml`, `api-gateway-deployment.yaml`
