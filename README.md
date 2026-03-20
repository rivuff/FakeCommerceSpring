# FakeCommerce

A RESTful e-commerce API built with **Spring Boot 4**, **Spring Data JPA**, **MySQL**, and **Flyway** migrations. It provides endpoints for managing products, categories, orders, and reviews with soft-delete support across all entities.

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Data JPA** (Hibernate)
- **MySQL 8+**
- **Flyway** for database migrations
- **Lombok** for boilerplate reduction
- **Gradle** (Groovy DSL)

## Prerequisites

- Java 21+
- MySQL 8+ running locally
- Gradle 8+ (or use the included Gradle wrapper)

## Setup Instructions

### 1. Clone the repository

```bash
git clone <repository-url>
cd FakeCommerce
```

### 2. Create the MySQL database

```sql
CREATE DATABASE fakecommerce;
```

### 3. Configure database credentials

Update `src/main/resources/application.yml` with your MySQL username and password:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fakecommerce
    username: <your-username>
    password: <your-password>
```

### 4. Run the application

```bash
./gradlew bootRun
```

The server starts on **http://localhost:8080**. Flyway will automatically run all migrations on startup.

## Database Migrations

| Migration | Description |
|---|---|
| `V1__init_schema.sql` | Creates `categories`, `products`, `orders`, and `order_products` tables |
| `V2__update_rating_to_decimal.sql` | Updates `products.rating` to `DECIMAL(3,1)` |
| `V3__add_reviews_table.sql` | Creates the `reviews` table with foreign keys to `products` and `orders` |

## Project Structure

```
src/main/java/com/example/FakeCommerce/
├── adapters/           # Entity-to-DTO mapping adapters
├── controllers/        # REST controllers
├── dtos/               # Request and response DTOs
├── exceptions/         # Custom exceptions and global handler
├── repositories/       # Spring Data JPA repositories
├── schema/             # JPA entity classes
├── services/           # Business logic layer
└── utils/              # Utility classes (ApiResponse wrapper)
```

## Features

- **CRUD operations** for Products, Categories, Orders, and Reviews
- **Soft deletes** across all entities using `@SQLDelete` and `@SQLRestriction`
- **Auditing** with auto-managed `createdAt` and `updatedAt` timestamps
- **Order management** with item-level actions (ADD, REMOVE, INCREMENT, DECREMENT)
- **Order summaries** with total item count and total price calculation
- **Review system** with lookup by product or order
- **Global exception handling** with structured error responses
- **Flyway migrations** for version-controlled schema management

## API Reference

All responses (except Review endpoints) are wrapped in a standard `ApiResponse` envelope:

```json
{
  "success": true,
  "message": "...",
  "error": null,
  "data": { }
}
```

---

### Categories — `/api/v1/categories`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/categories` | List all categories |
| `GET` | `/api/v1/categories/{id}` | Get category by ID |
| `POST` | `/api/v1/categories` | Create a new category |
| `DELETE` | `/api/v1/categories/{id}` | Delete a category (soft delete) |

**Create Category — Request Body:**

```json
{
  "name": "Electronics"
}
```

---

### Products — `/api/v1/products`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/products` | List all products |
| `GET` | `/api/v1/products/{id}` | Get product by ID |
| `GET` | `/api/v1/products/{id}/details` | Get product with category details |
| `POST` | `/api/v1/products` | Create a new product |
| `DELETE` | `/api/v1/products/{id}` | Delete a product (soft delete) |
| `GET` | `/api/v1/products/search?categoryName=` | Search products by category |
| `GET` | `/api/v1/products/categories` | List all distinct categories |

**Create Product — Request Body:**

```json
{
  "title": "iPhone 17",
  "description": "Latest Apple smartphone",
  "price": 80000.00,
  "image": "https://example.com/image.jpg",
  "categoryId": 1,
  "rating": 4.5
}
```

---

### Orders — `/api/v1/orders`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/orders` | List all orders |
| `GET` | `/api/v1/orders/{id}` | Get order by ID with items |
| `GET` | `/api/v1/orders/{id}/summary` | Get order summary with totals |
| `POST` | `/api/v1/orders` | Create a new order |
| `PUT` | `/api/v1/orders/{id}` | Update order status and/or items |
| `DELETE` | `/api/v1/orders/{id}` | Delete an order (soft delete) |

**Create Order — Request Body:**

```json
{
  "orderItems": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 1 }
  ]
}
```

**Update Order — Request Body:**

```json
{
  "status": "SHIPPED",
  "orderItems": [
    { "productId": 1, "quantity": 1, "action": "INCREMENT" },
    { "productId": 3, "quantity": 2, "action": "ADD" },
    { "productId": 2, "quantity": 0, "action": "REMOVE" }
  ]
}
```

**Order Item Actions:** `ADD`, `REMOVE`, `INCREMENT`, `DECREMENT`

**Order Statuses:** `PENDING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

---

### Reviews — `/api/v1/reviews`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/reviews` | List all reviews |
| `GET` | `/api/v1/reviews/{id}` | Get review by ID |
| `GET` | `/api/v1/reviews/product/{productId}` | Get all reviews for a product |
| `GET` | `/api/v1/reviews/order/{orderId}` | Get all reviews for an order |
| `POST` | `/api/v1/reviews` | Create a new review |
| `DELETE` | `/api/v1/reviews/{id}` | Delete a review (soft delete) |

**Create Review — Request Body:**

```json
{
  "comment": "Great product!",
  "rating": 4.5,
  "order": { "id": 1 },
  "product": { "id": 1 }
}
```

**Review Response:**

```json
{
  "id": 1,
  "productId": 1,
  "orderId": 1,
  "rating": 4.5,
  "comment": "Great product!",
  "createdAt": "2026-03-20T21:54:22.197127"
}
```

---

## Error Handling

The API uses a global exception handler that returns consistent error responses:

| Exception | HTTP Status | Description |
|---|---|---|
| `ResourceNotFoundException` | `404` | Requested resource does not exist |
| `BadRequestException` | `400` | Invalid request data |
| `ResourceDeletionException` | `409` | Conflict during resource deletion |
| Unhandled exceptions | `500` | Internal server error |

**Error Response Example:**

```json
{
  "success": false,
  "message": "Resource not found",
  "error": "Product with id 99 not found",
  "data": null
}
```

## Entity Relationships

```
Category 1──────* Product
Product  *──────* Order    (via OrderProducts join table)
Product  1──────* Review
Order    1──────* Review
```

## License

This project is for educational and demonstration purposes.
