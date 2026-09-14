# 🏛️ Cake Delight - Architecture & System Design

This document details the architectural topology, service communication patterns, data storage models, and deployment infrastructure of the **Cake Delight** microservices platform.

---

## 1. Overall System Architecture

Cake Delight is constructed following an **event-driven microservices architecture** pattern. All client requests (Web Storefront UI) enter the platform through a unified **API Gateway**, which validates JWT tokens and routes HTTP traffic to downstream business microservices. Asynchronous operations, such as order completion and email dispatch, are handled via **RabbitMQ** event messaging.

```mermaid
flowchart TD
    Client["Web Storefront / Browser"] -->|"HTTP / REST (Port 8080)<br/>Authorization: Bearer JWT"| Gateway["API Gateway (Port 8080)<br/>JWT Validation & Header Injection"]

    subgraph Business_Microservices["Business Microservices"]
        Gateway -->|"/api/auth"| User["User Service (Port 8085)<br/>X-User-Id, X-User-Role"]
        Gateway -->|"/api/catalog"| Catalog["Catalog Service (Port 8081)<br/>X-User-Id, X-User-Role"]
        Gateway -->|"/api/orders"| Order["Order Service (Port 8082)<br/>X-User-Id, X-User-Role"]
        Gateway -->|"/api/ratings"| Rating["Rating Service (Port 8083)<br/>X-User-Id, X-User-Role"]
        Gateway -->|"/api/notifications"| Notification["Notification Service (Port 8084)<br/>X-User-Id, X-User-Role"]
    end

    subgraph Data_Tier["Data Tier (MySQL 8.0)"]
        User -->|"JDBC"| MySQL["MySQL 8.0 (Host: 3307, K8s: 3306)<br/>Databases: user_db, cake_catalog, cake_order, cake_rating, notification_db"]
        Catalog -->|"JDBC"| MySQL
        Order -->|"JDBC"| MySQL
        Rating -->|"JDBC"| MySQL
        Notification -->|"JDBC"| MySQL
    end

    subgraph Messaging_Infrastructure["Messaging Infrastructure"]
        Order -->|"Publish OrderCompletedEvent"| RabbitMQ["RabbitMQ Broker (Port 5672 AMQP / 15672 UI)<br/>Exchange: cake-delight.exchange (Direct)"]
        RabbitMQ -->|"Routing Key: order.completed"| Queue["Queue: notification.order.completed"]
        Queue -->|"Consume Payload"| Notification
        Notification -.->|"Failure / Retry Exhaustion"| DLX["DLX: cake-delight.dlx"]
        DLX --> DLQ["DLQ: notification.order.completed.dlq"]
    end

    subgraph Email_Sink["Email Delivery Sink"]
        Notification -->|"SMTP (Port 1025)"| MailHog["MailHog Web UI (Port 8025)"]
    end
```

---

## 2. Component Topology & Responsibilities

| Service | Host / K8s Port | Database | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `8080` (NodePort `30080`) | None | Unified entry point, path routing, JWT authentication filter, header injection & static UI hosting. |
| **User Service** | `8085` | `user_db` | Manages user registration, login authentication, BCrypt password hashing, and JWT issuance. |
| **Catalog Service** | `8081` | `cake_catalog` | Manages cake catalog items, pricing, inventory stock, filtering, and RBAC admin mutations. |
| **Order Service** | `8082` | `cake_order` | Shopping basket, checkout processing, user-specific order history (`GET /api/orders`), order details & event publishing. |
| **Rating Service** | `8083` | `cake_rating` | Independent service managing customer cake reviews and aggregate average scores. |
| **Notification Service** | `8084` | `notification_db` | Consumes RabbitMQ order events, records notification audit logs, and dispatches emails via MailHog. |
| **MySQL** | Docker: `3307:3306`<br/>K8s: `3306` | Shared Instance | Relational storage hosting 5 isolated databases (`cake_catalog`, `cake_order`, `cake_rating`, `notification_db`, `user_db`). |
| **RabbitMQ** | `5672` (AMQP)<br/>`15672` (Web UI) | In-Memory / Disk | Asynchronous message broker handling direct exchanges (`cake-delight.exchange`), queues (`notification.order.completed`), and DLQ (`notification.order.completed.dlq`). |
| **MailHog** | `1025` (SMTP)<br/>`8025` (Web UI) | In-Memory | Local SMTP sink for receiving, inspecting, and debugging notification emails. |

---

## 3. Communication & Security Patterns

### A. JWT Authentication & Identity Propagation Flow
- The **API Gateway** serves as the security boundary for the entire microservice ecosystem.
- Client requests present `Authorization: Bearer <JWT>`.
- `JwtAuthenticationFilter` validates token signature and expiration against the shared `JWT_SECRET`.
- The Gateway strips unverified incoming identity headers and injects trusted downstream headers (`X-User-Id`, `X-User-Name`, `X-User-Role`).

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client / Storefront UI
    participant Gateway as API Gateway (8080)
    participant Auth as User Service (8085)
    participant Microservice as Downstream Service (8081-8084)

    Note over Client,Auth: Authentication Phase
    Client->>Gateway: POST /api/auth/login (username, password)
    Gateway->>Auth: Forward to /api/auth/login
    Auth->>Auth: Verify BCrypt password in user_db
    Auth-->>Client: Return Signed JWT Token (Bearer)

    Note over Client,Microservice: Authenticated Request Phase
    Client->>Gateway: GET /api/orders (Authorization: Bearer JWT)
    Gateway->>Gateway: Validate JWT signature & claims
    Gateway->>Gateway: Inject X-User-Id, X-User-Role, X-User-Name
    Gateway->>Microservice: Forward request with trusted X-User-Id header
    Microservice->>Microservice: Filter resources by X-User-Id / check RBAC
    Microservice-->>Client: Return 200 OK (User-Specific Data)
```

### B. User Order History Flow
- Endpoint: `GET /api/orders`
- Client sends `Authorization: Bearer <token>` without providing `userId` as a query parameter.
- API Gateway validates the JWT token and forwards `X-User-Id: <userId>` to `order-service`.
- `order-service` executes `orderRepository.findByUserIdOrderByOrderDateDesc(userId)` against `cake_order`.
- Returns user-specific order list sorted newest first.
- Individual order lookup (`GET /api/orders/{id}`) checks `order.getUserId().equals(userId)` or `ROLE_ADMIN`; returns `403 Forbidden` if requested by another user.

```mermaid
sequenceDiagram
    autonumber
    actor User as Authenticated User
    participant Gateway as API Gateway
    participant OrderService as Order Service
    participant DB as MySQL (cake_order)

    User->>Gateway: GET /api/orders (Bearer JWT)
    Gateway->>Gateway: Extract userId from JWT
    Gateway->>OrderService: GET /api/orders (Header: X-User-Id = userId)
    OrderService->>DB: findByUserIdOrderByOrderDateDesc(userId)
    DB-->>OrderService: Return Order Entities
    OrderService-->>User: 200 OK [ Newest Order, Older Order ]
```

### C. Asynchronous Event Messaging & DLQ Flow
- **Producer**: `order-service`
- **Exchange**: `cake-delight.exchange` (**Direct Exchange**)
- **Routing Key**: `order.completed`
- **Primary Queue**: `notification.order.completed`
- **Consumer**: `notification-service` (`OrderCompletedListener`)
- **Dead Letter Exchange (DLX)**: `cake-delight.dlx`
- **Dead Letter Queue (DLQ)**: `notification.order.completed.dlq`

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer / Browser
    participant Gateway as API Gateway (8080)
    participant Order as Order Service (8082)
    participant Rabbit as RabbitMQ Broker (5672)
    participant Notif as Notification Service (8084)
    participant Mail as MailHog (8025 / 1025)

    Customer->>Gateway: POST /api/orders/checkout (Bearer JWT)
    Gateway->>Order: Forward to /api/checkout (Header: X-User-Id)
    Order->>Order: Persist Order in cake_order & clear basket
    Order->>Rabbit: Publish OrderCompletedEvent to cake-delight.exchange (Routing Key: order.completed)
    Order-->>Customer: Return CheckoutResponse (201 Created)

    Rabbit->>Notif: Deliver from notification.order.completed queue
    Notif->>Notif: Idempotency check via eventId in notification_db
    alt SMTP Success
        Notif->>Mail: Send SMTP Email (Port 1025)
        Notif->>Notif: Set notification status = SENT
    else SMTP Failure / Exception
        Notif->>Notif: Set notification status = FAILED
        Notif-->>Rabbit: Reject message (triggers DLX cake-delight.dlx)
        Rabbit->>Rabbit: Route to notification.order.completed.dlq
    end
```

---

## 4. Database Schema & Data Isolation

Each microservice maintains strict database isolation within the shared MySQL server container:

```mermaid
erDiagram
    USERS {
        bigint id PK
        string username UK
        string email UK
        string password
        string role
        datetime created_at
    }

    CAKES {
        bigint id PK
        string name
        string description
        string category
        double price
        int stock
        string image_url
    }

    BASKET_ITEMS {
        bigint id PK
        bigint user_id
        bigint cake_id
        string cake_name
        double price_snapshot
        int quantity
    }

    ORDERS {
        bigint id PK
        bigint user_id
        double total_amount
        string status
        datetime order_date
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint cake_id
        string cake_name
        double price_snapshot
        int quantity
    }

    RATINGS {
        bigint id PK
        bigint cake_id
        bigint user_id
        int score
        string comment
        datetime created_at
    }

    NOTIFICATIONS {
        bigint id PK
        string event_id UK
        bigint order_id
        string channel
        string status
        datetime sent_at
        datetime created_at
        datetime updated_at
    }

    ORDERS ||--|{ ORDER_ITEMS : contains
```

---

## 5. Deployment Topologies

### Docker Compose View
All **9 containers** run within a single isolated bridge network `cake-network`. Service discovery relies on Docker container names (`api-gateway`, `user-service`, `catalog-service`, `order-service`, `rating-service`, `notification-service`, `cake-mysql`, `rabbitmq`, `mailhog`).
- **MySQL Host Port Mapping**: `3307:3306`
- **RabbitMQ Host Port Mappings**: AMQP `5672:5672`, Management UI `15672:15672`
- **MailHog Host Port Mappings**: SMTP `1025:1025`, Web UI `8025:8025`

### Kubernetes View
Deployed in namespace `cake-delight`:
- **Workload Summary**: 9 Deployments / Services, 11 Pods total.
- **Multi-Replica Deployments**:
  - `api-gateway`: **2 Replicas** (NodePort `30080` -> Target `8080`)
  - `user-service`: **2 Replicas** (ClusterIP `8085`)
- **Single-Replica Deployments**: `catalog-service`, `order-service`, `rating-service`, `notification-service`, `mysql`, `rabbitmq`, `mailhog` (**1 Replica each**).
- **Config & Secrets**: Managed globally via `cake-delight-config` ConfigMap and `cake-delight-secrets` Secret.
