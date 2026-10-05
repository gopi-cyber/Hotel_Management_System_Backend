# LuxeStay Hotel Management System - Backend

Spring Boot 3.2.5 REST API backend for the LuxeStay Hotel Management System.

## Tech Stack
- **Framework:** Spring Boot 3.2.5
- **Language:** Java 21
- **Database:** MySQL (Schema: `hotel_management`)
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security Crypto (BCrypt password hashing)
- **Build Tool:** Maven

## Package Structure
```
com.luxestay.hotel
├── config          # Security, CORS, and Data initialization
├── controller      # REST endpoints (users, rooms, staff, bookings, checkin, services)
├── dto             # Request and Response transfer objects
├── entity          # JPA Entities (User, Room, Staff, Booking, CheckInRecord, ServiceRequest)
├── repository      # Spring Data JPA repositories
└── service         # Business logic layer
```

## Setup & Running
1. Configure MySQL database details in `src/main/resources/application.properties`.
2. Run backend:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or package jar:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/hotel-backend-0.0.1-SNAPSHOT.jar
   ```
3. API runs on `http://localhost:8080`.
