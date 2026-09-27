# ShopMate 🛒

**ShopMate** is a modular, microservices-based e-commerce application designed to provide a scalable foundation for modern online shopping platforms.

The project follows a **Spring Boot microservices architecture**, separating core business capabilities into independently deployable services. It also includes an API Gateway, centralized configuration, authentication, notifications, payments, orders, products, and an AI-powered chatbot service.

---

## 🏗️ Architecture

ShopMate is organized into multiple independent services that communicate with each other through well-defined APIs.

```text
                         ┌─────────────────────┐
                         │      Client/UI      │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     API Gateway     │
                         └──────────┬──────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
       ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
       │ Auth Service│       │Product      │       │Order        │
       │             │       │Service      │       │Service      │
       └─────────────┘       └─────────────┘       └──────┬──────┘
                                                          │
                         ┌────────────────────────────────┼──────────────┐
                         │                                │              │
                         ▼                                ▼              ▼
                  ┌─────────────┐                 ┌─────────────┐ ┌─────────────┐
                  │Payment      │                 │Notification │ │Chatbot      │
                  │Service      │                 │Service      │ │Service      │
                  └─────────────┘                 └─────────────┘ └─────────────┘

                         ┌─────────────────────┐
                         │   Config Server     │
                         └─────────────────────┘

                         ┌─────────────────────┐
                         │     Common Lib      │
                         └─────────────────────┘
```

> The architecture is designed so individual services can be developed, tested, deployed, and scaled independently.

---

## 📦 Project Structure

```text
Shopmate/
│
├── api-gateway/
│   └── API Gateway for routing client requests
│
├── auth-service/
│   └── Authentication and authorization
│
├── chatbot-service/
│   └── AI-powered shopping assistant
│
├── common-lib/
│   └── Shared libraries and common components
│
├── config-server/
│   └── Centralized application configuration
│
├── notification-service/
│   └── Notification-related functionality
│
├── order-service/
│   └── Order management and processing
│
├── payment-service/
│   └── Payment processing
│
├── product-service/
│   └── Product catalog and product management
│
├── .idea/
│   └── IntelliJ IDEA project configuration
│
├── .gitignore
└── pom.xml
```

---

## 🔧 Services

### API Gateway

The API Gateway acts as the primary entry point for client requests.

Responsibilities include:

* Routing requests to appropriate microservices
* Providing a centralized entry point for APIs
* Supporting cross-cutting concerns such as security and request handling
* Hiding internal service implementation details from clients

---

### Authentication Service

The authentication service manages user authentication and authorization.

Potential responsibilities include:

* User authentication
* Authorization
* Token management
* Role-based access control
* Securing service endpoints

---

### Product Service

The Product Service manages the product catalog.

Typical responsibilities include:

* Product creation and management
* Product information
* Product search
* Product availability
* Product categorization

---

### Order Service

The Order Service handles the shopping order lifecycle.

Responsibilities include:

* Creating orders
* Updating order status
* Retrieving customer orders
* Managing order information
* Coordinating with payment and notification services

---

### Payment Service

The Payment Service is responsible for payment-related operations.

It is designed as an independent service so payment processing can evolve without tightly coupling payment logic to the order service.

---

### Notification Service

The Notification Service handles customer notifications.

Possible notification channels include:

* Email
* SMS
* Push notifications
* Order status notifications
* Payment notifications

---

### Chatbot Service 🤖

The Chatbot Service provides an AI-powered shopping assistant.

The service can be extended to support capabilities such as:

* Product recommendations
* Customer questions
* Product discovery
* Order-related questions
* Shopping assistance
* Natural-language interactions

---

### Config Server

The Config Server provides centralized configuration management for the microservices ecosystem.

Instead of maintaining configuration independently across every service, common configuration can be managed centrally.

This helps with:

* Environment-specific configuration
* Centralized property management
* Configuration consistency
* Easier deployment and maintenance

---

### Common Library

The Common Library contains reusable components shared between multiple services.

Examples may include:

* Common DTOs
* Exception handling
* Utility classes
* Shared constants
* Common API models

Keeping reusable functionality in one location reduces duplication across services.

---

## 🛠️ Technology Stack

The project is structured around a modern Java microservices ecosystem.

| Technology    | Purpose                                 |
| ------------- | --------------------------------------- |
| Java          | Primary programming language            |
| Spring Boot   | Microservice development                |
| Spring Cloud  | Microservices infrastructure            |
| Maven         | Build and dependency management         |
| REST APIs     | Service-to-service/client communication |
| API Gateway   | Centralized API routing                 |
| Config Server | Centralized configuration               |
| Git           | Source control                          |
| AI/LLM        | Chatbot capabilities                    |

Additional technologies such as databases, messaging systems, containerization, cloud infrastructure, and CI/CD can be integrated as the project evolves.

---

## 🚀 Getting Started

### Prerequisites

Before running ShopMate locally, make sure you have:

* Java 17+ installed
* Maven installed
* Git installed
* An IDE such as IntelliJ IDEA or Eclipse
* Required databases/services configured for the individual microservices

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

## 📥 Clone the Repository

```bash
git clone https://github.com/swastika238/Shopmate.git
```

Navigate to the project:

```bash
cd Shopmate
```

---

## 🔨 Build the Project

From the root directory:

```bash
mvn clean install
```

This builds the parent project and its modules.

---

## ▶️ Running the Services

Each microservice can be started independently.

For example:

```bash
cd config-server
mvn spring-boot:run
```

Then start the other services according to their dependencies.

A typical startup sequence is:

```text
1. Config Server
       ↓
2. Authentication Service
       ↓
3. Product Service
       ↓
4. Order Service
       ↓
5. Payment Service
       ↓
6. Notification Service
       ↓
7. Chatbot Service
       ↓
8. API Gateway
```

The exact startup order and ports may change as the project configuration evolves.

---

## 🔄 Example Order Flow

A typical shopping workflow can look like this:

```text
Customer
   │
   ▼
API Gateway
   │
   ▼
Authentication
   │
   ▼
Product Service
   │
   ▼
Create Order
   │
   ▼
Order Service
   │
   ├──────────────► Payment Service
   │
   └──────────────► Notification Service
```

For example:

1. A customer authenticates through the application.
2. The request reaches the API Gateway.
3. The Product Service provides product information.
4. The customer creates an order.
5. The Order Service processes the order.
6. The Payment Service handles payment processing.
7. The Notification Service sends relevant updates to the customer.

---

## 🤖 AI Shopping Assistant

ShopMate includes a dedicated `chatbot-service`, allowing AI functionality to remain separate from the core commerce services.

This architecture makes it possible to add AI capabilities without tightly coupling the chatbot implementation to product, order, or payment logic.

Future capabilities can include:

```text
Customer
    │
    ▼
Chatbot Service
    │
    ├──► Product Service
    │
    ├──► Order Service
    │
    └──► Recommendation / AI Engine
```

---

## 🔐 Security

Security is designed around the microservices architecture, with authentication handled through a dedicated service and requests passing through the API Gateway.

Security enhancements can include:

* JWT-based authentication
* Role-based authorization
* Secure API endpoints
* Service-to-service authentication
* Password encryption
* Input validation
* API request filtering

---

## 📈 Scalability

One of the primary advantages of the ShopMate architecture is independent service scalability.

For example:

```text
Product Service   → Scale based on product traffic
Order Service     → Scale based on order volume
Payment Service   → Scale based on payment requests
Chatbot Service   → Scale based on AI requests
Notification      → Scale based on notification volume
```

This allows individual services to scale without requiring the entire application to scale together.

---

## 🧪 Testing

Each service should maintain its own unit and integration tests.

Example:

```bash
mvn test
```

For a specific service:

```bash
cd product-service
mvn test
```

Testing can be expanded to include:

* Unit testing
* Integration testing
* API testing
* Contract testing
* End-to-end testing

---

## 📁 Multi-Module Maven Project

The root `pom.xml` manages the individual modules within the ShopMate project.

The repository is structured as a Maven multi-module project:

```text
Shopmate
│
├── api-gateway
├── auth-service
├── chatbot-service
├── common-lib
├── config-server
├── notification-service
├── order-service
├── payment-service
└── product-service
```

This structure makes it easier to maintain a consistent build and dependency-management strategy across the application.

---

## 🗺️ Future Enhancements

Potential future improvements include:

* [ ] Service discovery
* [ ] Centralized logging
* [ ] Distributed tracing
* [ ] Docker containerization
* [ ] Kubernetes deployment
* [ ] CI/CD pipeline
* [ ] Database integration
* [ ] Redis caching
* [ ] Kafka or RabbitMQ messaging
* [ ] API documentation with OpenAPI/Swagger
* [ ] JWT/OAuth2 security
* [ ] Product recommendation engine
* [ ] AI-powered shopping assistant
* [ ] Monitoring with Spring Boot Actuator
* [ ] Prometheus and Grafana monitoring
* [ ] Automated integration testing

---

## 📊 Microservices Benefits

ShopMate's architecture provides several benefits:

**Independent Deployment**

Each service can be deployed independently.

**Loose Coupling**

Business capabilities are separated into dedicated services.

**Scalability**

Individual services can be scaled based on demand.

**Maintainability**

Smaller services are easier to understand and maintain.

**Technology Flexibility**

Individual services can evolve independently while maintaining API contracts.

---

## 🤝 Contributing

Contributions are welcome.

To contribute:

```bash
git checkout -b feature/your-feature
```

Make your changes, commit them, and push the branch:

```bash
git add .
git commit -m "Add your feature"
git push origin feature/your-feature
```

Then open a Pull Request.

---

## 📄 License

This project is currently intended for learning, development, and demonstration purposes.

Add an appropriate open-source license to the repository if the project is intended for public redistribution.

---

## 👨‍💻 Project

**ShopMate**

A scalable microservices-based e-commerce platform with authentication, product management, orders, payments, notifications, centralized configuration, API Gateway, and AI chatbot capabilities.

**Repository:** `https://github.com/swastika238/Shopmate`
