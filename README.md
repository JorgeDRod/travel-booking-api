# Travel Booking API

A RESTful backend application built with **Java 17, Spring Boot, Spring Data JPA, and MySQL** to manage customers and their travel bookings.

This project demonstrates backend development practices, including layered architecture, CRUD operations, relational database management, request validation, centralized exception handling, and automated testing.

## Key Features

- **Customer Management:** Create, retrieve, update, and delete customer records.
- **Booking Management:** Create, retrieve, update, and delete travel bookings associated with customers.
- **Relational Database Design:** One-to-many relationship between customers and bookings using JPA and MySQL.
- **Request Validation:** Validate required fields, email formats, and booking dates.
- **DTO Architecture:** Use dedicated request and response DTOs to separate booking API representations from persistence entities.
- **Centralized Error Handling:** Return consistent JSON error responses for validation and business-rule failures.
- **Automated Testing:** 31 tests covering service logic, controller behavior, database persistence, and application startup, using JUnit, Mockito, MockMvc, and H2.

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |
| Version Control | Git, GitHub |

## Application Architecture

The application follows a layered architecture that separates HTTP request handling, business logic, and database operations.

```text
HTTP Client
    |
    v
Controller Layer
    |-- CustomerController
    |-- BookingController
    |      |-- BookingRequest / BookingResponse
    |      |-- BookingMapper
    |
    v
Service Layer
    |-- CustomerService
    |-- BookingService
    |
    v
Repository Layer
    |-- CustomerRepository
    |-- BookingRepository
    |
    v
MySQL Database
    |-- Customer
    |-- Booking
```

**Layer Responsibilities**

- **Controller:** Handles HTTP requests, validates request bodies, and returns appropriate HTTP responses.
- **DTO and Mapper:** Separates booking request/response representations from JPA entities and maps data between them.
- **Service:** Implements business rules, including checking customer existence and validating booking date ranges.
- **Repository:** Provides database access through Spring Data JPA and derived query methods.
- **Database:** Persists customers and bookings with a relational association.

For booking creation, the request flows from `BookingController` through `BookingMapper` to `BookingService`, which associates the booking with an existing customer and saves it using `BookingRepository`. The controller then maps the saved entity to a `BookingResponse`.

## Database Design

The application uses a relational database with two main entities: `Customer` and `Booking`.

### Entity Relationships

```text
Customer (1) ─────────── (Many) Booking
    |                              |
    | id (PK)                      | id (PK)
    | firstName                    | startDate
    | lastName                     | endDate
    | email                        | location
    | phone                        | customer_id (FK)
```

A customer can have multiple travel bookings, while each booking belongs to one customer.

The relationship is implemented using JPA annotations:

- `@OneToMany(mappedBy = "customer")` in the `Customer` entity.
- `@ManyToOne` and `@JoinColumn(name = "customer_id")` in the `Booking` entity.

The `Booking` table contains the `customer_id` foreign key referencing the `Customer` table.

Spring Data JPA and Hibernate manage the persistence of these entities.

### Data Integrity

The application enforces booking-date validation through the service layer. A booking cannot be created or updated with an end date earlier than its start date.

## REST API Endpoints

The API uses `/api/customers` as the base path for customer management.

### Customer Endpoints

| Method | Endpoint | Description | Success Status |
|---|---|---|---|
| POST | `/api/customers` | Create a customer | `200 OK` |
| GET | `/api/customers` | Retrieve all customers | `200 OK` |
| GET | `/api/customers/{id}` | Retrieve a customer by ID | `200 OK` |
| PUT | `/api/customers/{id}` | Update a customer | `200 OK` |
| DELETE | `/api/customers/{id}` | Delete a customer | `204 No Content` |

### Example: Create a Customer

**Request**

```http
POST /api/customers
Content-Type: application/json
```

```json
{
  "firstName": "Jorge",
  "lastName": "Rodriguez",
  "email": "jorge@example.com",
  "phone": "555-123-4567"
}
```

**Validation rules**

- `firstName` and `lastName` must not be blank.
- `email` is required and must have a valid format.
- `phone` is optional.

Invalid request bodies return `400 Bad Request` with validation error details.

### Booking Endpoints

All booking endpoints are nested under `/api/customers/{customerId}/bookings`.

| Method | Endpoint | Description | Success Status |
|---|---|---|---|
| POST | `/api/customers/{customerId}/bookings` | Create a booking for a customer | `201 Created` |
| GET | `/api/customers/{customerId}/bookings` | Retrieve all bookings for a customer | `200 OK` |
| GET | `/api/customers/{customerId}/bookings/{bookingId}` | Retrieve a specific booking | `200 OK` |
| PUT | `/api/customers/{customerId}/bookings/{bookingId}` | Update a booking | `200 OK` |
| DELETE | `/api/customers/{customerId}/bookings/{bookingId}` | Delete a booking | `204 No Content` |

### Example: Create a Booking

**Request**

```http
POST /api/customers/2/bookings
Content-Type: application/json
```

```json
{
  "startDate": "2027-06-01",
  "endDate": "2027-06-10",
  "location": "Rome"
}
```

**Example successful response — `201 Created`**

```json
{
  "id": 1,
  "startDate": "2027-06-01",
  "endDate": "2027-06-10",
  "location": "Rome"
}
```

The booking ID is generated by the database. The customer association is determined by the `{customerId}` path parameter rather than a customer object in the request body.

### Booking Validation

- `startDate` is required.
- `endDate` is required.
- `location` must not be blank.
- `endDate` cannot be earlier than `startDate`.
- The customer must exist before a booking can be created.

Invalid booking dates return `400 Bad Request`. Requests for nonexistent customers or bookings return `404 Not Found` where applicable.

### Example: Invalid Booking Dates

**Request**

```json
{
  "startDate": "2027-06-10",
  "endDate": "2027-06-01",
  "location": "Rome"
}
```

**Response — `400 Bad Request`**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "End date cannot be before start date."
}
```

## Getting Started

### Prerequisites

- Java 17 or later
- Maven (or the included Maven Wrapper)
- MySQL Server
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/JorgeDRod/travel-booking-api.git
cd travel-booking-api
```

### 2. Create the Database

Start MySQL and execute:

```sql
CREATE DATABASE travel_booking;
```

### 3. Configure Database Credentials

The application reads its database connection settings from environment variables.

| Variable | Description |
|---|---|
| `DB_URL` | MySQL JDBC connection URL |
| `DB_USERNAME` | MySQL database username |
| `DB_PASSWORD` | MySQL database password |

Example configuration:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/travel_booking}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
```

Set the `DB_PASSWORD` environment variable before starting the application. Set `DB_URL` and `DB_USERNAME` if your database configuration differs from the defaults.

**Security note:** Never commit database passwords or other secrets to GitHub.

### 4. Run the Application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The API runs locally at:

```text
http://localhost:8080
```

### 5. Test the API

Example request:

```http
GET http://localhost:8080/api/customers
```

You can test endpoints using Postman, curl, or another HTTP client.

## Automated Testing

The application includes **31 automated tests** covering service-layer business logic, REST controller behavior, database persistence, and application startup.

### Testing Frameworks

- **JUnit 5:** Test execution and assertions.
- **Mockito:** Mocking repository and service dependencies in unit tests.
- **MockMvc:** Testing REST endpoints, HTTP status codes, JSON responses, and validation behavior.
- **H2 Database:** In-memory database used for integration tests without requiring a running MySQL server.

### Test Coverage

| Test Class | Tests | Focus |
|---|---:|---|
| `BookingServiceTest` | 13 | Booking business logic, CRUD operations, invalid dates, and missing records |
| `BookingControllerTest` | 13 | HTTP endpoints, response status codes, validation, and error handling |
| `BookingIntegrationTest` | 4 | Application context, customer creation, booking persistence, and invalid-date rejection |
| `TravelBookingApiApplicationTests` | 1 | Spring application context startup |
| **Total** | **31** | **All tests passing** |

### Run Tests

On Windows:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
./mvnw test
```

Integration tests use the `test` Spring profile and an H2 in-memory database.

### Test Results

```text
Tests run: 31
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

The test suite verifies that invalid bookings are rejected before persistence, customer-booking relationships are correctly stored, and API endpoints return appropriate responses.
