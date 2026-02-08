# 🏪 ViperView Backend - Spring Boot Inventory Management API

> **UGAHacks 11 Backend** | Java Spring Boot REST API with AI Integration

## 📋 Overview

This is the backend service for ViperView, a retail inventory management system. Built with Java Spring Boot, it provides RESTful APIs for managing products, processing inventory transactions, tracking waste, and generating AI-powered analytics using Google Gemini.

**Key Focus**: This backend was developed as a **personal learning project** to master Spring Boot architecture, JPA/Hibernate, transaction management, and REST API design. Limited AI assistance was used - primarily for debugging specific issues while the architecture and implementation were done hands-on.

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Primary language |
| Spring Boot | 4.0.2 | Application framework |
| Spring Data JPA | - | ORM and data access |
| Hibernate | - | JPA implementation |
| PostgreSQL | - | Relational database |
| Supabase | - | Database hosting (AWS us-west-2) |
| Maven | - | Build tool |
| Lombok | - | Code generation |
| Jackson | - | JSON processing |
| Java HttpClient | - | AI API calls |
| Google Gemini | 1.5 Flash | AI-powered insights |

## 🏗️ Architecture

### Layer Structure

```
┌─────────────────────────────────────┐
│         Controllers                 │
│  (REST API Endpoints)               │
├─────────────────────────────────────┤
│         Services                    │
│  (Business Logic)                   │
├─────────────────────────────────────┤
│         Repositories                │
│  (Data Access Layer)                │
├─────────────────────────────────────┤
│         Entities/Models             │
│  (JPA Entity Classes)               │
└─────────────────────────────────────┘
```

### Design Principles

- **Separation of Concerns**: Controllers handle routing, services contain business logic, repositories manage data access
- **Transaction Safety**: `@Transactional` annotations ensure atomic operations
- **RESTful Design**: Clean URL structure following REST conventions
- **Type Safety**: Strong typing with Java generics and Lombok annotations
- **CORS Enabled**: Configured for frontend integration

## 📊 Database Schema

### Tables

#### products
```sql
id                UUID (PK)
barcode           VARCHAR (UNIQUE, NOT NULL)
name              VARCHAR
front_quantity    INTEGER (shelf stock)
back_quantity     INTEGER (backroom stock)
waste_quantity    INTEGER (total waste accumulated)
reorder_threshold INTEGER (alert threshold)
```

#### transactions
```sql
id                UUID (PK)
product_id        UUID (FK → products)
barcode           VARCHAR
product_name      VARCHAR
transaction_type  VARCHAR (CHECKOUT/RESTOCK/WASTE/SHIPMENT_RECEIVED)
quantity          INTEGER (negative = removed, positive = added)
location          VARCHAR (FRONT/BACK/BACK_TO_FRONT)
created_at        TIMESTAMP
```

#### waste_logs
```sql
id                UUID (PK)
barcode           VARCHAR
product_name      VARCHAR
quantity          INTEGER
location          VARCHAR (FRONT/BACK)
timestamp         TIMESTAMP
```

#### store_layout
```sql
id                UUID (PK)
product_id        UUID (FK → products)
aisle             VARCHAR (human-readable location)
x                 INTEGER (0-1000 coordinate system)
y                 INTEGER (0-1000 coordinate system)
```

## 🔌 API Endpoints

### Product Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/products` | GET | List all products |
| `/products` | POST | Add new product |
| `/products/barcode/{barcode}` | GET | Find product by barcode |

### Inventory Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/inventory/checkout` | POST | Process customer sale (decrease front stock) |
| `/inventory/restock` | POST | Move items from back → front |
| `/inventory/receive` | POST | Receive shipment from supplier (add to back) |
| `/inventory/waste` | POST | Log expired/damaged goods |
| `/inventory/waste` | GET | Get all waste history |
| `/inventory/waste/{barcode}` | GET | Get waste history for product |

### Transaction History

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/inventory/transactions/recent` | GET | Last 50 transactions (all products) |
| `/inventory/transactions/barcode/{barcode}` | GET | Transaction history by barcode |
| `/inventory/transactions/product/{id}` | GET | Transaction history by product UUID |

### AI Analytics

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/analytics/ai-insights` | GET | Get Gemini-powered inventory analysis |

### Digital Twin / Floor View

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/floorview` | GET | Get store layout with real-time inventory pressure |

## 💡 Core Features

### 1. Atomic Inventory Transactions ✅

**Technology**: Spring `@Transactional` annotations

**Purpose**: Ensures data consistency across multi-step operations

**Example**: When restocking, the operation decrements back storage AND increments front shelves atomically. If any part fails, the entire transaction rolls back.

```java
@Transactional
public Map<String, Object> restock(TransactionRequest request) {
    // Decrement back
    product.setBackQuantity(product.getBackQuantity() - request.getQuantity());
    // Increment front
    product.setFrontQuantity(product.getFrontQuantity() + request.getQuantity());
    // Log transaction
    transactionRepository.save(transaction);
    // Save product
    productRepository.save(product);
    // All or nothing!
}
```

### 2. Barcode-Based Operations 🔍

- Products identified by unique barcode
- All operations (checkout, restock, waste) use barcode as input
- Designed for integration with USB barcode scanners

### 3. Inventory Flow Tracking 📦

Four distinct transaction types:

1. **CHECKOUT**: Customer purchase (front stock → customer)
2. **RESTOCK**: Internal transfer (back stock → front stock)
3. **WASTE**: Loss/expiration (front/back → waste log)
4. **SHIPMENT_RECEIVED**: Supplier delivery (supplier → back stock)

### 4. Real-Time Alerts ⚠️

- **Restock Alert**: Triggered when front stock ≤ threshold AND back stock > 0
- **Supplier Order Alert**: Triggered when back stock ≤ threshold
- Returned in API responses for immediate frontend display

### 5. Waste Analytics 🗑️

- Every waste event logged with timestamp and location
- Separate `waste_logs` table for sustainability tracking
- Aggregate waste metrics calculated in AI analytics

### 6. AI-Powered Insights 🤖

**Technology**: Google Gemini 1.5 Flash API

**Process**:
1. Backend fetches last 50 transactions + all products
2. Builds analytics context (transaction summary, inventory status, waste %)
3. Sends structured prompt to Gemini API
4. Receives AI-generated insights (patterns, urgent actions, optimization tips)
5. Returns to frontend for display

**AI Prompt Structure**:
```
You are an AI Logistics Manager...
Analyze: [transaction data, inventory status, waste metrics]
Provide:
- Key Insights (2-3 bullet points)
- Urgent Actions (1-2 items)
- Optimization Tips (2-3 tips)
- Predictions (based on trends)
```

**Example Output**:
```
Key Insights:
- Milk shows high checkout velocity with 45 transactions
- Waste rate is 8.5%, slightly above optimal threshold

Urgent Actions:
- Restock 'Bread' immediately (front quantity: 2)

Optimization Tips:
- Implement FIFO rotation for perishables
- Monitor 'Milk' closely — high turnover

Predictions:
- Current checkout rate suggests 'Milk' depleted in 3 days
```

### 7. Digital Twin / FloorView 🏪

**Purpose**: Map physical store layout with real-time inventory pressure

**Data Structure**:
```json
{
  "productId": "uuid",
  "productName": "Milk",
  "barcode": "001",
  "aisle": "A1",
  "x": 100,
  "y": 200,
  "frontQuantity": 8,
  "frontThreshold": 10,
  "backQuantity": 45,
  "stockRatio": 0.8,           // Backend-calculated
  "restockNeeded": true,        // Backend flag
  "backroomReorderNeeded": false,
  "highWasteZone": false,       // AI detection
  "highTrafficZone": true,      // AI detection
  "aiInsight": "Restock recommended soon"
}
```

**Backend Intelligence**:
- Calculates `stockRatio = frontQuantity / threshold`
- Flags products needing restock
- Detects high-waste zones (>2 waste events)
- Detects high-traffic zones (>5 checkouts)
- Generates per-product AI insights

## ⚙️ Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres
spring.datasource.username=postgres.hsmeiabexlqizyellkba
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### CORS Configuration
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### AI Configuration
```properties
gemini.api.key=AIzaSyCz0f72nuWvZ4c6tOMaDYp4YPNjVuXRIk4
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent
```

## 📦 Data Transfer Objects (DTOs)

### TransactionRequest
```java
{
  barcode: String,
  quantity: int,
  location: String  // "FRONT" or "BACK" (for waste operations)
}
```

### FloorViewDTO
```java
{
  productId, productName, barcode, aisle, x, y,
  frontQuantity, frontThreshold, backQuantity,
  stockRatio, restockNeeded, backroomReorderNeeded,
  aiInsight, highWasteZone, highTrafficZone
}
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL (or Supabase account)
- Google Gemini API key

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd UGAHacks11-backend
   ```

2. **Configure database**

   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://<your-db-host>:5432/<your-db-name>
   spring.datasource.username=<your-username>
   spring.datasource.password=<your-password>
   ```

3. **Add Gemini API key**
   ```properties
   gemini.api.key=<your-gemini-api-key>
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`

### Testing

Use the included `test.http` file with IntelliJ IDEA's HTTP client or any REST client:

```http
### Get all products
GET http://localhost:8080/products

### Add a product
POST http://localhost:8080/products
Content-Type: application/json

{
  "barcode": "123456",
  "name": "Milk",
  "frontQuantity": 10,
  "backQuantity": 50,
  "wasteQuantity": 0,
  "reorderThreshold": 5
}

### Process checkout
POST http://localhost:8080/inventory/checkout
Content-Type: application/json

{
  "barcode": "123456",
  "quantity": 2
}
```

## 📁 Project Structure

```
UGAHacks11-backend/
├── src/main/java/com/example/ugahacks11backend/
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── InventoryController.java
│   │   ├── AnalyticsController.java
│   │   └── FloorViewController.java
│   ├── service/
│   │   ├── InventoryService.java
│   │   ├── GeminiService.java
│   │   └── FloorViewService.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── TransactionRepository.java
│   │   ├── WasteLogRepository.java
│   │   └── StoreLayoutRepository.java
│   ├── model/
│   │   ├── Product.java
│   │   ├── Transaction.java
│   │   ├── WasteLog.java
│   │   └── StoreLayout.java
│   ├── dto/
│   │   ├── TransactionRequest.java
│   │   └── FloorViewDTO.java
│   ├── config/
│   │   ├── DataSeeder.java
│   │   └── LayoutSeeder.java
│   ├── WebConfig.java
│   └── UgaHacks11BackendApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── test.http
└── pom.xml
```

## 🧪 Testing Strategy

- **HTTP Client Testing**: `test.http` file for endpoint testing
- **Data Seeding**: `DataSeeder` and `LayoutSeeder` auto-populate demo data
- **Transaction Flow Testing**: Checkout → Restock → Waste workflows
- **Demo-Ready**: Database seeds automatically if empty

## 🎯 Key Design Decisions

### 1. Service Layer Separation
- Controllers are thin (routing only)
- Business logic isolated in services
- Testable, maintainable, follows SOLID principles

### 2. Transaction History as First-Class Data
- Not just logs — structured data for analytics
- Enables velocity calculations, trend analysis
- Powers AI predictions

### 3. Location-Aware Inventory
- Front (shelf) vs Back (storage) distinction
- Critical for retail operations
- Enables restocking workflow optimization

### 4. Backend-Calculated Metrics
- `stockRatio` calculated server-side
- Consistent business logic
- Frontend displays, doesn't compute

### 5. AI as Enrichment, Not Control
- AI provides insights, not commands
- Backend flags recommendations
- Humans make final decisions

## 📈 Future Enhancements

- **Caching**: Redis for FloorView endpoint (high-read, low-write)
- **Message Queue**: Kafka/RabbitMQ for async transaction logging
- **Microservices**: Split inventory, analytics, AI into separate services
- **WebSockets**: Real-time dashboard updates
- **Load Balancing**: Multiple backend instances behind NGINX

## 📚 Learning Outcomes

This backend represents a deep dive into:
- Spring Boot application architecture
- JPA/Hibernate entity design and relationships
- Transaction management with `@Transactional`
- RESTful API design principles
- Service layer patterns
- AI API integration
- Database schema design for retail operations
- CORS configuration for frontend integration

---

**Built for UGAHacks 11** | **Spring Boot Learning Journey** | **Hands-On Implementation**