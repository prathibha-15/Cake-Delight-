# 🏛️ Cake Delight - Architecture & System Design

This document details the architectural topology, service communication patterns, data storage models, and deployment infrastructure of the **Cake Delight** microservices platform.

---

## 1. Overall System Architecture

Cake Delight is constructed following an **event-driven microservices architecture** pattern. All client requests (Web Storefront UI) enter the platform through a unified **API Gateway**, which routes HTTP traffic to downstream business microservices. Asynchronous operations, such as order completion and email dispatch, are handled via **RabbitMQ** event messaging.

```mermaid
flowchart TD
    Client["💻 Web Storefront / Client (Browser)"] -->|HTTP / REST (Port 8080)| Gateway["🌐 API Gateway (Spring Cloud Gateway)"]

    subgraph Business Microservices
        Gateway -->|/api/catalog/**| Catalog["🍰 Catalog Service (Port 8081)"]
        Gateway -->|/api/orders/**| Order["🛒 Order Service (Port 8082)"]
        Gateway -->|/api/ratings/**| Rating["⭐ Rating Service (Port 8083)"]
        Gateway -->|/api/notifications/**| Notification["🔔 Notification Service (Port 8084)"]
    end

    subgraph Data Tier
        Catalog -->|JDBC| MySQL[("🗄️ MySQL 8.0 (Port 3307:3306)\n- cake_catalog\n- cake_order\n- cake_rating\n- notification_db")]
        Order -->|JDBC| MySQL
        Rating -->|JDBC| MySQL
        Notification -->|JDBC| MySQL
    end

    subgraph Event & Messaging Infrastructure
        Order -->|Publish OrderCompletedEvent| RabbitMQ["🐇 RabbitMQ (Port 5672 / 15672)\nExchange: order.events.exchange"]
        RabbitMQ -->|Consume order.completed.queue| Notification
    end

    subgraph Email Delivery Sink
        Notification -->|SMTP (Port 1025)| MailHog["📬 MailHog (Port 8025 Web UI)"]
    end
```

---

## 2. Component Topology & Responsibilities

| Service | Port | Database | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `8080` | None | Unified reverse-proxy entry point, request path routing, static UI hosting. |
| **Catalog Service** | `8081` | `cake_catalog` | Manages cake catalog items, pricing, inventory stock, and filtering by category/name/price. |
| **Order Service** | `8082` | `cake_order` | Manages shopping basket items, checkout processing, order records, and AMQP event publishing. |
| **Rating Service** | `8083` | `cake_rating` | Manages customer cake ratings, review comments, and calculates aggregate average scores. |
| **Notification Service** | `8084` | `notification_db` | Consumes RabbitMQ order events, records notification audit logs, and sends emails via MailHog. |
| **MySQL** | `3307` | Shared Instance | Relational storage hosting 4 isolated databases (`cake_catalog`, `cake_order`, `cake_rating`, `notification_db`). |
| **RabbitMQ** | `5672`, `15672` | In-Memory / Disk | Asynchronous message broker handling topic exchanges and durable queues. |
| **MailHog** | `1025`, `8025` | In-Memory | Local SMTP sink for receiving, inspecting, and debugging notification emails. |

---

## 3. Communication Patterns

### A. UI to Backend Routing
- The SPA Web Storefront static assets (`index.html`) are served directly by the **API Gateway** at `http://localhost:8080/`.
- All API requests from the frontend use relative paths starting with `/api/`.
- Spring Cloud Gateway filters rewrite routes dynamically:
  - `/api/catalog/**` -> rewritten & forwarded to `http://catalog-service:8081/api/cakes/**`
  - `/api/orders/basket` -> rewritten & forwarded to `http://order-service:8082/api/basket`
  - `/api/orders/checkout` -> rewritten & forwarded to `http://order-service:8082/api/checkout`
  - `/api/ratings/**` -> rewritten & forwarded to `http://rating-service:8083/api/ratings/**`
  - `/api/notifications/**` -> forwarded to `http://notification-service:8084/api/notifications/**`

### B. Asynchronous Event Messaging
- **Producer**: `order-service`
- **Exchange**: `order.events.exchange` (Topic Exchange)
- **Routing Key**: `order.completed`
- **Queue**: `order.completed.queue`
- **Consumer**: `notification-service` (`OrderCompletedListener`)

When checkout is executed, `order-service` saves the order state to `cake_order.orders`, clears the basket, and broadcasts `OrderCompletedEvent` to RabbitMQ. `notification-service` consumes the payload asynchronously without blocking the checkout HTTP response.

---

## 4. End-to-End Order & Notification Flow

```mermaid
sequenceDiagram
    autonumber
    actor Customer as 👤 Customer / Browser
    participant Gateway as 🌐 API Gateway (8080)
    participant Catalog as 🍰 Catalog Service (8081)
    participant Order as 🛒 Order Service (8082)
    participant Rabbit as 🐇 RabbitMQ Broker
    participant Notif as 🔔 Notification Service (8084)
    participant Mail as 📬 MailHog (8025)

    Customer->>Gateway: GET /api/catalog/cakes
    Gateway->>Catalog: Forward request to /api/cakes
    Catalog-->>Customer: Return Cake Catalog List

    Customer->>Gateway: POST /api/orders/basket (cakeId: 1, quantity: 2)
    Gateway->>Order: Forward to /api/basket
    Order-->>Customer: Return BasketItemResponse (201 Created)

    Customer->>Gateway: POST /api/orders/checkout
    Gateway->>Order: Forward to /api/checkout
    Order->>Order: Persist Order & Clear Basket
    Order->>Rabbit: Publish OrderCompletedEvent to order.events.exchange
    Order-->>Customer: Return CheckoutResponse (Order Placed)

    Rabbit->>Notif: Deliver message from order.completed.queue
    Notif->>Notif: Save notification record (PENDING -> SENT)
    Notif->>Mail: Send SMTP Email to MailHog (Port 1025)
    Mail-->>Customer: View email in MailHog Web UI (Port 8025)
```

---

## 5. Database Schema & Data Isolation

Each microservice maintains strict database isolation within the shared MySQL server container:

```mermaid
erDiagram
    CAKES {
        bigint id PK
        varchar name
        varchar description
        varchar category
        double price
        int stock
        varchar image_url
    }

    BASKET_ITEMS {
        bigint id PK
        bigint cake_id
        varchar cake_name
        double price_snapshot
        int quantity
        double subtotal
    }

    ORDERS {
        bigint order_id PK
        double total_amount
        varchar status
        datetime order_date
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint cake_id
        varchar cake_name
        double price
        int quantity
    }

    RATINGS {
        bigint id PK
        bigint cake_id
        bigint user_id
        int score
        varchar comment
        datetime created_at
    }

    NOTIFICATIONS {
        bigint id PK
        uuid event_id UK
        bigint order_id
        varchar channel
        varchar status
        datetime sent_at
        datetime created_at
        datetime updated_at
    }

    ORDERS ||--|{ ORDER_ITEMS : contains
```

---

## 6. Deployment Topologies

### Docker Compose View
All 8 containers run within a single isolated bridge network `cake-network`. Service discovery relies on Docker container names (`catalog-service`, `order-service`, `rating-service`, `notification-service`, `cake-mysql`, `rabbitmq`, `mailhog`).

### Kubernetes View
Deployed in namespace `cake-delight`:
- **API Gateway**: Deployed with `NodePort` mapping `30080:8080`.
- **Microservices & Infrastructure**: Deployed as individual `Deployment` resources paired with `ClusterIP` `Service` definitions.
- **Config & Secrets**: Managed globally via `cake-delight-config` ConfigMap and `cake-delight-secret` Secret.
