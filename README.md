
# 🏦 Pismo  Demo

## 🛠️ Tech Stack

* **Java:** 21
* **Framework:** Spring Boot 3.2.2
* **Database:** H2 (In-Memory)
* **Containerization:** Docker
* **Documentation:** Swagger UI (OpenAPI 3)
* **Build Tool:** Maven

---

## 🚀 Getting Started

You can run this application locally or inside a Docker container.

### Prerequisites
* Java 21
* Docker Desktop (optional, for containerization)

### Option 1: Run Locally
Use the Maven wrapper included in the project:

```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw spring-boot:run
```
### Option 2: Run with Docker


Open CLI & Run these commands

* ./mvnw clean package -DskipTests
* docker build -t pismo-demo .
* docker run -p 8080:8080 pismo-demo

---

API Documentation
Once the application is running, you can access the interactive documentation and database console:

### Swagger UI http://localhost:8080/swagger-ui.html

### H2 Console	http://localhost:8080/h2-console
* User: sa
* Pass: password
* JDBC URL: jdbc:h2:mem:demodb
---
# Quick Test
### Create an Account:
curl -X POST http://localhost:8080/accounts \
-H "Content-Type: application/json" \
-d '{"document_number": "12345678900"}'

### Get account
curl -X 'GET' \
'http://localhost:8080/accounts/1' \
-H 'accept: */*'

### Create a Transaction:
curl -X POST http://localhost:8080/transactions \
-H "Content-Type: application/json" \
-d '{
"account_id": 1,
"operation_type_id": 1,
"amount": 123.45
}'


