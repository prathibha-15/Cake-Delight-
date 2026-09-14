# 🍰 Cake Delight - Microservices Enterprise Platform

**Cake Delight** is an event-driven microservices application designed for an online cake ordering platform. Built with **Java 17**, **Spring Boot** (3.3.3 Gateway / 4.1.0 Services), **Spring Cloud Gateway**, **Spring Security**, **MySQL 8.0**, **Flyway**, **RabbitMQ**, and **MailHog**, the system supports containerized execution via **Docker Compose** and orchestration via **Kubernetes**.

---

## 📦 Source Code & Repository

- **GitHub Repository**: [https://github.com/prathibha-15/Cake-Delight-](https://github.com/prathibha-15/Cake-Delight-)

The GitHub repository contains the complete project source code, microservice implementations, Docker Compose setup, Kubernetes manifests, and technical documentation.

---

## 🏛️ Architecture & System Topology

The platform consists of 6 core application microservices and 3 supporting infrastructure containers:

| Component | Container / Service Name | Host Port | Internal Port | Database | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **API Gateway** | `api-gateway` | `8080` | `8080` | None | Spring Cloud Gateway routing, JWT validation & static UI hosting. |
| **User Service** | `user-service` | `8085` | `8085` | `user_db` | Manages user registration, BCrypt password hashing & JWT authentication. |
| **Catalog Service** | `catalog-service` | `8081` | `8081` | `cake_catalog` | Manages cake catalog items, categories, pricing, stock & filtering. |
| **Order Service** | `order-service` | `8082` | `8082` | `cake_order` | Shopping basket, checkout, order history & event publishing. |
| **Rating Service** | `rating-service` | `8083` | `8083` | `cake_rating` | Customer cake reviews, scores & aggregate average rating calculation. |
| **Notification Service** | `notification-service` | `8084` | `8084` | `notification_db` | Consumes RabbitMQ order events & dispatches confirmation emails. |
| **MySQL Database** | `cake-mysql` | `3307` | `3306` | Shared Instance | Relational storage for 5 isolated databases (`cake_catalog`, `cake_order`, `cake_rating`, `notification_db`, `user_db`). |
| **RabbitMQ Broker** | `rabbitmq` | `5672`, `15672` | `5672`, `15672` | In-Memory / Disk | AMQP Direct Exchange broker & Web Management Dashboard. |
| **MailHog Server** | `mailhog` | `1025`, `8025` | `1025`, `8025` | In-Memory | Local SMTP sink & Web Inbox for viewing order confirmation emails. |

For detailed architectural sequence flows, security identity propagation, and ER diagrams, see [docs/architecture.md](docs/architecture.md).

---

## 🛠️ Technology Stack & Prerequisites

### Tech Stack
- **Java**: Java 17 LTS (Eclipse Temurin)
- **Framework**: Spring Boot (3.3.3 for API Gateway, 4.1.0 for Application Microservices), Spring Cloud Gateway, Spring Security
- **Security & Authentication**: JJWT 0.12.6, BCrypt Password Encoding
- **Persistence**: Spring Data JPA / Hibernate (`ddl-auto=validate`), MySQL 8.0, Flyway Schema Migrations
- **Messaging**: RabbitMQ (AMQP 0-9-1 Direct Exchange, DLX/DLQ)
- **Email Sink**: MailHog (SMTP)
- **API Specs**: SpringDoc OpenAPI (Swagger UI)
- **Frontend**: Vanilla HTML5, CSS3, JavaScript (ES6+ fetch API, JWT session management)
- **Containerization & Orchestration**: Docker, Docker Compose (9 containers), Kubernetes (Minikube, Kustomize)

### Prerequisites
- **Docker Desktop** (or Docker Engine + Docker Compose)
- **Java 17 JDK** & **Apache Maven 3.9+** (optional, required only for standalone local development/testing)
- **kubectl** & **Minikube** (optional, required only for Kubernetes deployment)

---

## 🔐 Authentication & Security Architecture

The platform enforces a centralized Gateway authentication model with zero-trust downstream headers:

- **User Registration**: `POST /api/auth/register` (Creates user entry in `user_db` with BCrypt password hashing).
- **User Login**: `POST /api/auth/login` (Authenticates credentials and returns a signed JWT token containing `id`, `username`, and `role`).
- **Gateway Identity Verification**: API Gateway's `JwtAuthenticationFilter` intercepts all incoming requests, validates the JWT signature and expiration, and extracts the user identity claims.
- **Identity Propagation**: The Gateway injects verified headers (`X-User-Id`, `X-User-Name`, `X-User-Role`) into requests forwarded to downstream microservices. Client-supplied identity headers are stripped/overwritten by the Gateway to prevent header spoofing.
- **Role-Based Access Control (RBAC)**:
  - `ROLE_ADMIN`: Allowed full mutation privileges on the catalog (`POST`, `PUT`, `DELETE` on `/api/catalog/cakes`).
  - `ROLE_USER`: Access to normal shopping, basket, checkout, order history, ratings, and notifications.
  - **Unauthorized Access (401)**: Returned when accessing protected endpoints without a valid JWT.
  - **Forbidden Access (403)**: Returned when a user attempts operations without sufficient role permissions (e.g. non-admin attempting cake creation) or accesses resources belonging to another user.

---

## 📜 User Order History & Ownership Security

- **Order History Endpoint**: `GET /api/orders`
  - Retrieves all historical orders belonging to the authenticated user, sorted newest first (`findByUserIdOrderByOrderDateDesc`).
  - Does **NOT** accept `userId` as a query parameter from the frontend client. The server strictly derives the user identity from the verified `X-User-Id` header injected by the Gateway.
  - Returns `200 OK` with an empty array `[]` if the user has no orders.
- **Individual Order Ownership**: `GET /api/orders/{id}`
  - Enforces strict ownership checks. If User A attempts to request User B's order ID (`GET /api/orders/{UserBOrderId}`), the Order Service rejects the request with **HTTP 403 Forbidden**.

---

## 🚀 Quick Start Guide (Docker Compose - Recommended)

After extracting the repository, open a terminal in the `cake-delight` directory before running the commands below.

All microservices use multi-stage Docker builds, so no local Maven installation or host compilation step is required.

### Step 1: Start All Infrastructure & Microservices
Launch all containers in detached mode using Docker Compose:

```bash
docker-compose up --build -d
```

### Step 2: Verify Container Health
Check that all **9 containers** are running and healthy:

```bash
docker-compose ps
```

---

## 🖥️ Web User Interfaces & Management Dashboards

Once Docker Compose is running, access the following web interfaces in your browser:

- 🛒 **Storefront UI**: [http://localhost:8080](http://localhost:8080)
- 📬 **MailHog Email Inbox**: [http://localhost:8025](http://localhost:8025)
- 🐇 **RabbitMQ Dashboard**: [http://localhost:15672](http://localhost:15672) *(Credentials: `guest` / `guest`)*

---

## 📖 Swagger / OpenAPI Documentation

Each backend microservice includes built-in SpringDoc Swagger UI documentation accessible on its native port:

| Microservice | Interactive Swagger UI URL | Raw OpenAPI JSON |
| :--- | :--- | :--- |
| **Catalog Service** | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs) |
| **Order Service** | [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) | [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs) |
| **Rating Service** | [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) | [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs) |
| **Notification Service** | [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html) | [http://localhost:8084/v3/api-docs](http://localhost:8084/v3/api-docs) |
| **User Service** | [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html) | [http://localhost:8085/v3/api-docs](http://localhost:8085/v3/api-docs) |

---

## 📡 API Endpoints Reference

All client API requests should be routed through the **API Gateway** on port `8080`.

### 🔑 Authentication API
- `POST http://localhost:8080/api/auth/register` - Register a new user (`{"username": "user1", "email": "user1@example.com", "password": "password123"}`).
- `POST http://localhost:8080/api/auth/login` - Authenticate user and receive JWT token (`{"username": "user1", "password": "password123"}`).

### 🍰 Catalog API
- `GET http://localhost:8080/api/catalog/cakes` - Retrieve all cakes. Optional query params: `?category=`, `?name=`, `?minPrice=`, `?maxPrice=`.
- `GET http://localhost:8080/api/catalog/cakes/{id}` - Get cake details by ID.
- `POST http://localhost:8080/api/catalog/cakes` - Create a new cake entry (*Requires ROLE_ADMIN*).
- `PUT http://localhost:8080/api/catalog/cakes/{id}` - Update existing cake details (*Requires ROLE_ADMIN*).
- `DELETE http://localhost:8080/api/catalog/cakes/{id}` - Delete a cake entry (*Requires ROLE_ADMIN*).

### 🛒 Order & Basket API
- `GET http://localhost:8080/api/orders/basket` - Retrieve current shopping basket (*Requires Auth*).
- `POST http://localhost:8080/api/orders/basket` - Add cake to basket (`{"cakeId": 1, "quantity": 2}`) (*Requires Auth*).
- `PUT http://localhost:8080/api/orders/basket/{itemId}` - Update quantity of basket item (*Requires Auth*).
- `DELETE http://localhost:8080/api/orders/basket/{itemId}` - Remove item from basket (*Requires Auth*).
- `POST http://localhost:8080/api/orders/checkout` - Checkout active basket and place an order (*Requires Auth*).
- `GET http://localhost:8080/api/orders` - Retrieve authenticated user's complete order history, sorted newest first (*Requires Auth*).
- `GET http://localhost:8080/api/orders/{id}` - Get order status by order ID (*Requires ownership or ROLE_ADMIN; 403 Forbidden otherwise*).

### ⭐ Rating API
- `POST http://localhost:8080/api/ratings` - Submit a cake review (`{"cakeId": 1, "userId": 1, "score": 5, "comment": "Delicious!"}`) (*Requires Auth*).
- `GET http://localhost:8080/api/ratings/cakes/{cakeId}` - Get all customer reviews for a cake.
- `GET http://localhost:8080/api/ratings/cakes/{cakeId}/average` - Get average score & total review count.

### 🔔 Notification API
- `GET http://localhost:8080/api/notifications/{orderId}` - Get notification records for a specific order (*Requires Auth*).

---

## 📝 Verified Request & Response Payload Examples

### 1. User Registration & Login
**Registration Endpoint:** `POST http://localhost:8080/api/auth/register`  
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "password123"
}
```
**Login Endpoint:** `POST http://localhost:8080/api/auth/login`  
**Response (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "role": "ROLE_USER"
}
```

### 2. Add Item to Shopping Basket
**Endpoint:** `POST http://localhost:8080/api/orders/basket`  
**Headers:** `Authorization: Bearer <token>`  
**Response (`201 Created`):**
```json
{
  "id": 1,
  "cakeId": 1,
  "cakeName": "Chocolate Truffle",
  "priceSnapshot": 799.0,
  "quantity": 2,
  "subtotal": 1598.0
}
```

### 3. Checkout Basket
**Endpoint:** `POST http://localhost:8080/api/orders/checkout`  
**Headers:** `Authorization: Bearer <token>`  
**Response (`201 Created`):**
```json
{
  "message": "Order placed successfully",
  "order": {
    "orderId": 1,
    "totalAmount": 1598.0,
    "status": "CREATED",
    "orderDate": "2026-09-12T10:57:23.393151"
  }
}
```

### 4. Fetch User Order History
**Endpoint:** `GET http://localhost:8080/api/orders`  
**Headers:** `Authorization: Bearer <token>`  
**Response (`200 OK`):**
```json
[
  {
    "orderId": 2,
    "totalAmount": 799.0,
    "status": "CREATED",
    "orderDate": "2026-09-12T10:58:10.780229",
    "items": [
      {
        "id": 2,
        "cakeId": 1,
        "cakeName": "Chocolate Truffle",
        "priceSnapshot": 799.0,
        "quantity": 1
      }
    ]
  },
  {
    "orderId": 1,
    "totalAmount": 1598.0,
    "status": "CREATED",
    "orderDate": "2026-09-12T10:57:23.393151",
    "items": [
      {
        "id": 1,
        "cakeId": 1,
        "cakeName": "Chocolate Truffle",
        "priceSnapshot": 799.0,
        "quantity": 2
      }
    ]
  }
]
```

---

## 🐇 Event-Driven Messaging & RabbitMQ Architecture

Cake Delight uses an asynchronous AMQP event publication pattern for order completions:

- **Exchange**: `cake-delight.exchange` (**Direct Exchange**)
- **Routing Key**: `order.completed`
- **Primary Queue**: `notification.order.completed` (Durable)
- **Dead Letter Exchange (DLX)**: `cake-delight.dlx`
- **Dead Letter Queue (DLQ)**: `notification.order.completed.dlq`

### Order-to-Notification Flow:
1. User executes `POST /api/orders/checkout`.
2. `order-service` saves the order in `cake_order`, clears the basket, and broadcasts `OrderCompletedEvent` to `cake-delight.exchange` with routing key `order.completed`.
3. `notification-service` (`OrderCompletedListener`) consumes the message asynchronously from `notification.order.completed`.
4. Consumer verifies idempotency using `eventId` (UUID). If new, it records a `PENDING` entry in `notification_db`, transmits an email via SMTP to MailHog, and updates the notification status to `SENT`.
5. If MailHog/SMTP is unreachable, the exception is caught and recorded as status `FAILED` in `notification_db`. Unhandled listener exceptions trigger Spring AMQP retry handling and dead-letter routing to `notification.order.completed.dlq`.

---

## 💾 Database Architecture & Data Isolation

The platform enforces a strict **database-per-service** pattern within the MySQL server:

- `cake_catalog`: Managed by `catalog-service` (tables: `cakes`).
- `cake_order`: Managed by `order-service` (tables: `orders`, `order_items`, `basket_items`).
- `cake_rating`: Managed by `rating-service` (tables: `ratings`).
- `notification_db`: Managed by `notification-service` (tables: `notifications`).
- `user_db`: Managed by `user-service` (tables: `users`).

### Schema Management & Migrations:
- **Flyway Migrations**: Each service manages version-controlled SQL migration scripts (`src/main/resources/db/migration/V1__...sql`, `V2__...sql`).
- **JPA Validation**: Hibernate is configured with `spring.jpa.hibernate.ddl-auto=validate` to verify that entity models strictly match Flyway-managed schema versions.

---

## 🎨 Single-Page Storefront Frontend

The frontend is a lightweight Single-Page Application (SPA) built using Vanilla HTML5, CSS3, and JavaScript (ES6+ fetch API):

- **Location**: Served directly by API Gateway at `http://localhost:8080/`.
- **JWT Session Restoration**: Automatically restores user sessions from `localStorage` token on page reload.
- **Authentication**: Modal dialogs for Login and Registration.
- **Order History View**: Displays the logged-in user's historic orders (Order ID, Status, Total Amount, Date/Time, Items, Notification Status), sorted newest first.
- **Ratings & Reviews**: Renders ratings with reviewer names (`👤 <username>` for logged-in user, `👤 Customer` for others).
- **Admin UI**: Displays the Cake Management section exclusively for users with `ROLE_ADMIN`, providing controls to add, edit, or delete cakes.

---

## 🛡️ Fault Tolerance & Resilience Scenarios

The system has been verified under isolated failure conditions:

1. **Notification Service Offline**: Orders are successfully processed by `order-service`. Messages accumulate safely in durable queue `notification.order.completed` until `notification-service` recovers.
2. **MailHog Service Offline**: `notification-service` processes the order event, logs a `FAILED` notification record in `notification_db`, and prevents transaction rollback for order placement.
3. **Catalog Service Offline**: Gateway returns `503 Service Unavailable` for catalog queries; existing basket and order history remain accessible.
4. **RabbitMQ Broker Offline**: Order placement falls back gracefully; database commits succeed and background message retry policies kick in once RabbitMQ reconnects.
5. **Recovery Verification**: Post-restoration tests confirm 100% message delivery and clean state reconciliation without duplicate processing.

---

## ☸️ Kubernetes Deployment Guide (`k8s/`)

This section provides complete instructions for deploying **Cake Delight** on a local **Minikube** cluster.

### 📋 Kubernetes Topology Summary

- **Namespace**: `cake-delight`
- **Deployments & Services (9 Workloads, 11 Pods Total)**:
  - `api-gateway`: **2 Replicas** (NodePort `30080` -> Target `8080`)
  - `user-service`: **2 Replicas** (ClusterIP `8085`)
  - `catalog-service`: **1 Replica** (ClusterIP `8081`)
  - `order-service`: **1 Replica** (ClusterIP `8082`)
  - `rating-service`: **1 Replica** (ClusterIP `8083`)
  - `notification-service`: **1 Replica** (ClusterIP `8084`)
  - `mysql`: **1 Replica** (ClusterIP `3306`, PVC `mysql-pvc` 2Gi)
  - `rabbitmq`: **1 Replica** (ClusterIP `5672` / `15672`)
  - `mailhog`: **1 Replica** (ClusterIP `1025` / `8025`)
- **Probes**: Configured with `startupProbe` (up to 10 min JVM warm-up window for MySQL initialization), `readinessProbe`, and `livenessProbe`.

---

### ⚡ Complete Deployment Sequence (Windows PowerShell)

#### Step 1: Start Minikube Cluster
```powershell
minikube start --cpus 4 --memory 6144
```

#### Step 2: Point PowerShell Session to Minikube's Docker Daemon
```powershell
& minikube -p minikube docker-env --shell powershell | Invoke-Expression
```

#### Step 3: Build Microservice Docker Images
Build all 6 application images inside Minikube's Docker daemon using `Dockerfile.fast`:

```powershell
docker build -t cake-delight-api-gateway:latest -f ./api-gateway/Dockerfile.fast ./api-gateway
docker build -t cake-delight-user-service:latest -f ./user-service/Dockerfile.fast ./user-service
docker build -t cake-delight-catalog-service:latest -f ./catalog-service/Dockerfile.fast ./catalog-service
docker build -t cake-delight-order-service:latest -f ./order-service/Dockerfile.fast ./order-service
docker build -t cake-delight-rating-service:latest -f ./rating-service/Dockerfile.fast ./rating-service
docker build -t cake-delight-notification-service:latest -f ./notification-service/Dockerfile.fast ./notification-service
```

#### Step 4: Deploy Kubernetes Manifests
Apply all manifests using Kustomize:

```powershell
kubectl apply -k k8s/
```

#### Step 5: Verify Deployment Status
```powershell
kubectl get pods -n cake-delight
```
Wait until all **11 pods** transition to **`1/1 Running`**.

---

### 🌐 Cluster Access & Port Forwarding

Kubernetes ClusterIP services require port-forwarding to be accessed from host browser/Postman:

1. **API Gateway & Storefront UI** (`http://localhost:8080`):
   ```powershell
   kubectl port-forward service/api-gateway 8080:8080 -n cake-delight
   ```
2. **MailHog Web Inbox** (`http://localhost:8025`):
   ```powershell
   kubectl port-forward service/mailhog 8025:8025 -n cake-delight
   ```
3. **RabbitMQ Dashboard** (`http://localhost:15672` - Credentials: `guest` / `guest`):
   ```powershell
   kubectl port-forward service/rabbitmq 15672:15672 -n cake-delight
   ```
