# Cake Delight - Microservices Application

**Cake Delight** is a enterprise-grade microservices application designed for an online cake ordering platform. The system is built with **Java 17**, **Spring Boot 3**, **Spring Cloud Gateway**, **MySQL 8.0**, **RabbitMQ**, and **MailHog**, and supports orchestration via both **Docker Compose** and **Kubernetes**.

---

## 🏛️ Architecture Overview

The application comprises 5 core microservices and 3 supporting infrastructure services:

| Component | Port | Description |
| :--- | :--- | :--- |
| **API Gateway** (`api-gateway`) | `8080` | Spring Cloud Gateway routing requests & serving static UI. |
| **Catalog Service** (`catalog-service`) | `8081` | Manages cake catalog, categories, pricing, stock & filtering. |
| **Order Service** (`order-service`) | `8082` | Handles basket management, checkout & publishes order events. |
| **Rating Service** (`rating-service`) | `8083` | Manages cake reviews, scores, and calculates average ratings. |
| **Notification Service** (`notification-service`) | `8084` | Consumes RabbitMQ events & sends emails via MailHog. |
| **MySQL** (`cake-mysql`) | `3307:3306` | Relational database containing isolated databases per service. |
| **RabbitMQ** | `5672`, `15672` | AMQP Message Broker for asynchronous event-driven messaging. |
| **MailHog** | `1025`, `8025` | Local SMTP server & Web UI for inspecting notification emails. |

For detailed architectural flow and database ER diagrams, see [docs/architecture.md](docs/architecture.md).

---

## 🚀 Quick Start (Docker Compose)

### Prerequisites
- **Java 17 JDK**
- **Apache Maven 3.9+**
- **Docker Desktop**

### 1. Build and Package Microservices
Run the following command from the root directory to compile and package all microservices:

```bash
cd catalog-service && mvn clean package -DskipTests && cd ..
cd order-service && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
cd notification-service && mvn clean package -DskipTests && cd ..
cd rating-service && mvn clean package -DskipTests && cd ..
```

### 2. Start Services with Docker Compose
Launch all containers in detached mode:

```bash
docker-compose up --build -d
```

### 3. Verify Running Services
Access the following web interfaces in your browser:
- **Web Storefront**: [http://localhost:8080](http://localhost:8080)
- **MailHog Email Inbox**: [http://localhost:8025](http://localhost:8025)
- **RabbitMQ Dashboard**: [http://localhost:15672](http://localhost:15672) (Guest / Guest)

---

## ☸️ Kubernetes Deployment (`k8s/`)

Deploy the complete stack to a Kubernetes cluster (e.g. Minikube / Docker Desktop K8s):

```bash
# Apply ConfigMaps, Secrets, PVC, and Deployments
kubectl apply -f k8s/
```

Check deployment status:
```bash
kubectl get pods
kubectl get services
```

---

## 🧪 Running Unit Tests

Run unit tests across all microservices using Maven:

```bash
cd catalog-service && mvn test && cd ..
cd order-service && mvn test && cd ..
cd rating-service && mvn test && cd ..
cd notification-service && mvn test && cd ..
```

---

## 📡 Key API Endpoints Reference

All requests can be routed through the API Gateway at `http://localhost:8080`.

### Catalog API
- `GET /api/catalog/cakes` - Get all cakes (Supports `?category=`, `?name=`, `?minPrice=`, `?maxPrice=`)
- `GET /api/catalog/cakes/{id}` - Get cake details by ID

### Order & Basket API
- `GET /api/orders/basket` - Retrieve current shopping basket
- `POST /api/orders/basket` - Add cake to basket (`{"cakeId": 1, "quantity": 2}`)
- `DELETE /api/orders/basket/{itemId}` - Remove item from basket
- `POST /api/orders/checkout` - Checkout basket and place order
- `GET /api/orders/orders/{id}` - Get order status by ID

### Rating API
- `POST /api/ratings` - Submit a cake rating (`{"cakeId": 1, "userId": 101, "score": 5, "comment": "Great!"}`)
- `GET /api/ratings/cakes/{cakeId}` - Get all ratings for a cake
- `GET /api/ratings/cakes/{cakeId}/average` - Get average score & review count for a cake

### Notification API
- `GET /api/notifications/orders/{orderId}` - Get notification records for an order
