# 🍰 Cake Delight - Microservices Enterprise Platform

**Cake Delight** is an event-driven microservices application designed for an online cake ordering platform. Built with **Java 17**, **Spring Boot 3**, **Spring Cloud Gateway**, **MySQL 8.0**, **RabbitMQ**, and **MailHog**, the system supports containerized execution via **Docker Compose** and orchestration via **Kubernetes**.

---

## 🏛️ Architecture & System Topology

The platform consists of 5 core application microservices and 3 supporting infrastructure containers:

| Component | Container / Service Name | Host Port | Internal Port | Description |
| :--- | :--- | :--- | :--- | :--- |
| **API Gateway** | `api-gateway` | `8080` | `8080` | Spring Cloud Gateway routing requests & serving static UI frontend. |
| **Catalog Service** | `catalog-service` | `8081` | `8081` | Manages cake catalog items, categories, pricing, stock, & filtering. |
| **Order Service** | `order-service` | `8082` | `8082` | Handles shopping basket management, checkout, & publishes order events. |
| **Rating Service** | `rating-service` | `8083` | `8083` | Manages customer cake reviews, scores, and calculates average ratings. |
| **Notification Service** | `notification-service` | `8084` | `8084` | Consumes RabbitMQ order events & dispatches order confirmation emails. |
| **MySQL Database** | `cake-mysql` | `3307` | `3306` | Relational database hosting isolated databases per service (`cake_catalog`, `cake_order`, `cake_rating`, `notification_db`). |
| **RabbitMQ Broker** | `rabbitmq` | `5672`, `15672` | `5672`, `15672` | AMQP Message Broker & Web Management Dashboard. |
| **MailHog Server** | `mailhog` | `1025`, `8025` | `1025`, `8025` | Local SMTP sink & Web Inbox for viewing order confirmation emails. |

For detailed architectural sequence flows and ER diagrams, see [docs/architecture.md](docs/architecture.md).

---

## 🛠️ Technology Stack & Prerequisites

### Tech Stack
- **Java**: Java 17 LTS (Eclipse Temurin)
- **Framework**: Spring Boot 3.3.x, Spring Cloud Gateway
- **Persistence**: Spring Data JPA / Hibernate, MySQL 8.0
- **Messaging**: RabbitMQ (AMQP 0-9-1)
- **Email Sink**: MailHog (SMTP)
- **API Specs**: SpringDoc OpenAPI (Swagger UI)
- **Containerization**: Docker, Docker Compose, Kubernetes (Kustomize)

### Prerequisites
- **Java 17 JDK** or higher
- **Apache Maven 3.9+**
- **Docker Desktop** (or Docker Engine + Docker Compose)
- **kubectl** & **Minikube** (optional, required only for Kubernetes deployment)

---

## 🚀 Quick Start Guide (Docker Compose - Recommended)

### Step 1: Package Microservices
Run Maven from the root directory to compile and build JAR artifacts for all microservices:

```bash
cd catalog-service && mvn clean package -DskipTests && cd ..
cd order-service && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
cd notification-service && mvn clean package -DskipTests && cd ..
cd rating-service && mvn clean package -DskipTests && cd ..
```

### Step 2: Start All Infrastructure & Microservices
Launch all containers in detached mode using Docker Compose:

```bash
docker-compose up --build -d
```

### Step 3: Verify Container Health
Check that all 8 containers are running and healthy:

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

---

## 📡 API Endpoints Reference

All client API requests should be routed through the **API Gateway** on port `8080`.

### 🍰 Catalog API
- `GET http://localhost:8080/api/catalog/cakes` - Retrieve all cakes. Optional query params: `?category=`, `?name=`, `?minPrice=`, `?maxPrice=`.
- `GET http://localhost:8080/api/catalog/cakes/{id}` - Get cake details by ID.
- `POST http://localhost:8080/api/catalog/cakes` - Create a new cake entry (Admin).
- `PUT http://localhost:8080/api/catalog/cakes/{id}` - Update existing cake details.
- `DELETE http://localhost:8080/api/catalog/cakes/{id}` - Delete a cake entry.

### 🛒 Order & Basket API
- `GET http://localhost:8080/api/orders/basket` - Retrieve current shopping basket.
- `POST http://localhost:8080/api/orders/basket` - Add cake to basket (`{"cakeId": 1, "quantity": 2}`).
- `PUT http://localhost:8080/api/orders/basket/{itemId}` - Update quantity of basket item.
- `DELETE http://localhost:8080/api/orders/basket/{itemId}` - Remove item from basket.
- `POST http://localhost:8080/api/orders/checkout` - Checkout basket and place an order.
- `GET http://localhost:8080/api/orders/orders/{id}` - Get order status by order ID.

### ⭐ Rating API
- `POST http://localhost:8080/api/ratings` - Submit a cake review (`{"cakeId": 1, "userId": 101, "score": 5, "comment": "Delicious!"}`).
- `GET http://localhost:8080/api/ratings/cakes/{cakeId}` - Get all customer reviews for a cake.
- `GET http://localhost:8080/api/ratings/cakes/{cakeId}/average` - Get average score & total review count.

### 🔔 Notification API
- `GET http://localhost:8080/api/notifications/{orderId}` - Get notification records for a specific order.

---

## 📝 Verified Request & Response Payload Examples

### 1. Add Item to Shopping Basket
**Endpoint:** `POST http://localhost:8080/api/orders/basket`  
**Request Body:**
```json
{
  "cakeId": 1,
  "quantity": 2
}
```
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

### 2. Checkout Basket
**Endpoint:** `POST http://localhost:8080/api/orders/checkout`  
**Response (`201 Created`):**
```json
{
  "message": "Order placed successfully",
  "order": {
    "orderId": 1,
    "totalAmount": 1598.0,
    "status": "CREATED",
    "orderDate": "2026-08-10T13:53:31.366033661"
  }
}
```

### 3. Submit Cake Rating
**Endpoint:** `POST http://localhost:8080/api/ratings`  
**Request Body:**
```json
{
  "cakeId": 1,
  "userId": 101,
  "score": 5,
  "comment": "Amazing Truffle Cake!"
}
```
**Response (`201 Created`):**
```json
{
  "id": 1,
  "cakeId": 1,
  "userId": 101,
  "score": 5,
  "comment": "Amazing Truffle Cake!",
  "createdAt": "2026-08-10T13:53:35.769926132"
}
```

---

## 🧪 Automated Testing & Postman Collection

### Automated E2E Test Script
Run the built-in end-to-end verification script from the root directory to automatically test the complete order-to-notification lifecycle:

- **Windows (PowerShell / CMD):**
  ```powershell
  .\test-flow.bat
  ```
- **Linux / macOS / Git Bash:**
  ```bash
  chmod +x test-flow.sh
  ./test-flow.sh
  ```

### Postman Collection
An automated Postman collection is included in the codebase:
- File path: [docs/Cake-Delight-Postman-Collection.json](docs/Cake-Delight-Postman-Collection.json)

**How to use:**
1. Open **Postman**.
2. Click **Import** -> Select `docs/Cake-Delight-Postman-Collection.json`.
3. The collection imports with the `{{baseUrl}}` variable set to `http://localhost:8080`.
4. Run individual requests or execute the full collection runner.

---

## 📧 How to Verify Email Notification Delivery

1. Execute a checkout via the web UI at `http://localhost:8080` or via `POST /api/orders/checkout`.
2. `order-service` publishes an `OrderCompletedEvent` to RabbitMQ exchange `order.events.exchange`.
3. `notification-service` consumes the message from `order.completed.queue`, records the email in `notification_db.notifications`, and transmits an HTML email via SMTP to MailHog.
4. Open **MailHog Web UI** at **[http://localhost:8025](http://localhost:8025)** to inspect the received order confirmation email.

---

## ☸️ Kubernetes Deployment Guide (`k8s/`)

Deploy the stack to a local Kubernetes cluster (e.g. Minikube):

### 1. Apply Manifests
Deploy all secrets, configmaps, persistent volumes, deployments, and services under namespace `cake-delight`:

```bash
kubectl apply -f k8s/
```

### 2. Verify Pod Status
Check that all pods in namespace `cake-delight` achieve `1/1 Running`:

```bash
kubectl get pods -n cake-delight
```

### 3. Expose & Access Services
The API Gateway is configured as a `NodePort` service mapping port `8080` to node port `30080`.

- **Access via Minikube Service URL:**
  ```bash
  minikube service api-gateway -n cake-delight
  ```
- **Access via Port Forwarding:**
  ```bash
  kubectl port-forward svc/api-gateway 8080:8080 -n cake-delight
  ```

---

## 🛠️ Troubleshooting Common Startup Issues

| Symptom | Cause | Resolution |
| :--- | :--- | :--- |
| `Port 8080/8081/3307 already in use` | Another process is bound to the port. | Stop existing processes or run `docker-compose down`. |
| `MySQL connection refused` on microservice startup | MySQL is still initializing databases. | Wait 15 seconds; Docker Compose healthchecks will restart dependent services automatically. |
| `RabbitMQ connection refused` | Broker container not ready yet. | Check `docker logs rabbitmq` to ensure management plugin is active. |
| `404 Not Found` on `/api/...` endpoints | Incorrect gateway path or port. | Ensure requests are sent to API Gateway port `8080`. |
| MailHog inbox empty after checkout | Notification service disabled or RabbitMQ host mismatch. | Check `docker logs notification-service` to confirm event consumption. |
