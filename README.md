# 🍰 Cake Delight - Microservices Enterprise Platform

**Cake Delight** is an event-driven microservices application designed for an online cake ordering platform. Built with **Java 17**, **Spring Boot 3**, **Spring Cloud Gateway**, **MySQL 8.0**, **RabbitMQ**, and **MailHog**, the system supports containerized execution via **Docker Compose** and orchestration via **Kubernetes**.

---

## 📦 Source Code & Repository

- **GitHub Repository**: [https://github.com/prathibha-15/Cake-Delight-](https://github.com/prathibha-15/Cake-Delight-)

The GitHub repository contains the complete project source code, microservice implementations, Docker Compose setup, Kubernetes manifests, and technical documentation.

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
- **Docker Desktop** (or Docker Engine + Docker Compose)
- **Java 17 JDK** & **Apache Maven 3.9+** (optional, required only for standalone local development/testing)
- **kubectl** & **Minikube** (optional, required only for Kubernetes deployment)

---

## 🚀 Quick Start Guide (Docker Compose - Recommended)

All microservices use multi-stage Docker builds, so no local Maven installation or host compilation step is required.

### Step 1: Start All Infrastructure & Microservices
Launch all containers in detached mode using Docker Compose:

```bash
docker-compose up --build -d
```

### Step 2: Verify Container Health
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

This section provides complete step-by-step instructions for deploying and verifying the **Cake Delight** microservices stack on a local **Minikube** Kubernetes cluster from a clean environment.

---

### 📋 Kubernetes Prerequisites

Before beginning, ensure your local system has the following software installed:

- **Docker Desktop** (must be installed and **RUNNING** before starting Minikube, as Minikube uses the Docker container driver).
- **Minikube** (v1.30+).
- **kubectl** (Kubernetes command-line interface).
- **Windows PowerShell** (or bash for Linux/macOS).
- **Sufficient System Resources**: Allocate at least **4 CPUs** and **8 GB RAM** to Docker Desktop / Minikube to run all 8 containers smoothly.

> [!IMPORTANT]
> - You **do NOT need to manually install or start MySQL, RabbitMQ, or MailHog** on your host machine. Kubernetes manifests deploy MySQL, RabbitMQ, MailHog, and all five microservices automatically into the cluster.
> - You **do NOT need to manually build Maven JAR files** on your host machine before deployment. Multi-stage Docker builds inside Minikube compile and package the services automatically.
> - Running `minikube start` initializes the Kubernetes cluster environment only. Running `kubectl apply -k k8s/` deploys the actual Cake Delight application stack into the cluster.

---

### ⚡ Complete Deployment Sequence (Windows PowerShell)

Follow this exact sequence in Windows PowerShell to build and deploy the complete system:

#### Step 1: Start Docker Desktop
Ensure Docker Desktop is open and running on your host machine.

#### Step 2: Start Minikube Cluster
Launch Minikube using the default Docker driver:

```powershell
minikube start
```

#### Step 3: Configure PowerShell Session to use Minikube's Docker Daemon
Direct your current PowerShell session to build Docker images inside Minikube's internal Docker daemon:

```powershell
& minikube -p minikube docker-env --shell powershell | Invoke-Expression
```

> [!NOTE]
> This command must be executed in the active PowerShell terminal where you run the image build commands.

#### Step 4: Build Application Docker Images
Build all five microservice Docker images directly inside Minikube's environment:

```powershell
docker build -t cake-delight-api-gateway:latest ./api-gateway
docker build -t cake-delight-catalog-service:latest ./catalog-service
docker build -t cake-delight-order-service:latest ./order-service
docker build -t cake-delight-rating-service:latest ./rating-service
docker build -t cake-delight-notification-service:latest ./notification-service
```

#### Step 5: Verify Built Images
Confirm that all 5 application images exist inside Minikube's Docker daemon:

```powershell
docker images | Select-String "cake-delight"
```

#### Step 6: Deploy Complete Kubernetes Stack
Apply all secrets, configmaps, persistent volume claims, deployments, and services using Kustomize:

```powershell
kubectl apply -k k8s/
```

#### Step 7: Monitor Deployment & Check Pod Status
Check the status of all pods in namespace `cake-delight`:

```powershell
kubectl get pods -n cake-delight
```

**Expected Pod List (8 Pods Total):**
- `api-gateway`
- `catalog-service`
- `order-service`
- `rating-service`
- `notification-service`
- `mysql`
- `rabbitmq`
- `mailhog`

Wait until all 8 pods transition to **`1/1 Running`** with **`RESTARTS = 0`** (Spring Boot startup and initial database pool setup takes ~1–3 minutes).

To inspect cluster service definitions, internal IPs, and target port mappings:

```powershell
kubectl get services -n cake-delight
```

---

### 🌐 Accessing the Application

Kubernetes ClusterIP services are internal to the Minikube cluster. To interact with the storefront, APIs, and Web UIs from your host browser or Postman, use `kubectl port-forward`.

> [!NOTE]
> Port-forwarding commands do **NOT** start the services. Kubernetes starts the containers when `kubectl apply -k k8s/` is executed. Port-forwarding simply bridges an already-running Kubernetes service/deployment to `localhost`.

1. **Access API Gateway & Storefront UI** (`http://localhost:8080`):
   ```powershell
   kubectl port-forward deployment/api-gateway 8080:8080 -n cake-delight
   ```
2. **Access MailHog Web Inbox** (`http://localhost:8025`):
   ```powershell
   kubectl port-forward service/mailhog 8025:8025 -n cake-delight
   ```
3. **Access RabbitMQ Management Dashboard** (`http://localhost:15672` - Credentials: `guest` / `guest`):
   ```powershell
   kubectl port-forward service/rabbitmq 15672:15672 -n cake-delight
   ```

---

### 🔍 Verification & Functional End-to-End Testing

#### 1. Cluster Status & Log Inspection Commands
Run the following commands to check pod health and trace the asynchronous event-driven workflow:

```powershell
# Verify all pods are Ready 1/1
kubectl get pods -n cake-delight

# View cluster service ports
kubectl get services -n cake-delight

# Inspect Order Service logs
kubectl logs deployment/order-service -n cake-delight --tail=50

# Inspect Notification Service logs (verify RabbitMQ event consumption)
kubectl logs deployment/notification-service -n cake-delight --tail=50

# Inspect RabbitMQ Broker logs
kubectl logs deployment/rabbitmq -n cake-delight --tail=30
```

#### 2. Functional Verification Workflow
Follow these steps to perform end-to-end verification of the deployed stack:

1. **Open API Gateway**: Navigate to `http://localhost:8080` in your browser.
2. **Fetch Catalog Cakes**: Send `GET http://localhost:8080/api/catalog/cakes` to verify items are returned from MySQL.
3. **Add Cake to Basket**: Send `POST http://localhost:8080/api/orders/basket` (`{"cakeId": 1, "quantity": 2}`).
4. **Checkout Order**: Send `POST http://localhost:8080/api/orders/checkout` (`{"customerName": "Tester", "customerEmail": "demo@cakedelight.local", "shippingAddress": "123 Main St"}`).
5. **Verify Order Created**: Confirm HTTP 201 response containing `"status": "CREATED"` and `orderId`.
6. **Verify Order Service Event**: Inspect `order-service` logs to confirm `OrderCreatedEvent` was published to RabbitMQ exchange `order.events.exchange`.
7. **Verify Notification Listener**: Inspect `notification-service` logs to confirm receipt of `OrderCreatedEvent` from queue `order.completed.queue`.
8. **Verify Email Inbox**: Open MailHog UI at `http://localhost:8025` to view the order confirmation email (`Subject: [Cake Delight] Order #...`).

---

### 🛠️ Troubleshooting Common Kubernetes Issues

| Symptom | Probable Cause | Exact Resolution |
| :--- | :--- | :--- |
| `minikube start` fails | Docker Desktop is not running or responsive. | Start Docker Desktop, wait for it to report "Engine running", then re-run `minikube start`. |
| `ImagePullBackOff` or `ErrImagePull` on application pods | Images were built on the host Docker daemon rather than inside Minikube's Docker environment. | Run `& minikube -p minikube docker-env --shell powershell \| Invoke-Expression` in your terminal, then rebuild all 5 docker images using `docker build -t cake-delight-<service>:latest ./<service>`. |
| Pods show `0/1 Running` or restart during initial startup | Spring Boot JPA & MySQL database pool initialization is warming up. | Kubernetes `startupProbe` provides up to 10 minutes for JVM startup. Wait 1–2 minutes for startup probes to complete; pods will transition to `1/1 Running`. |
| Browser shows `Connection Refused` on `http://localhost:8080` | Port-forwarding command has not been started in a terminal. | Run `kubectl port-forward deployment/api-gateway 8080:8080 -n cake-delight` in a separate terminal. |
| MailHog inbox inaccessible at `http://localhost:8025` | Port-forwarding command has not been started for MailHog. | Run `kubectl port-forward service/mailhog 8025:8025 -n cake-delight` in a separate terminal. |
