# Certificate Verification System

A secure and scalable certificate verification platform built using Spring Boot, designed to issue, manage, and verify academic certificates with QR-code based validation, verification logging, and role-based access control.

---

# Features

* User authentication and authorization
* Role-based access control (Admin/User)
* Institution management
* Certificate issuance and verification
* QR Code generation
* Verification logging
* Activity auditing
* Risk score detection
* Tampered certificate detection
* RESTful API architecture
* Database persistence using JPA/Hibernate

---

# Tech Stack

## Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

## Database

* PostgreSQL / MySQL

## Build Tool

* Maven

## Other

* JWT Authentication
* QR Code Generation
* REST APIs

---

# Project Structure

```text
src/
 ├── main/
 │   ├── java/com/acd/verify/
 │   │   ├── controller/
 │   │   ├── service/
 │   │   ├── repository/
 │   │   ├── model/
 │   │   ├── security/
 │   │   └── config/
 │   └── resources/
 │       └── application.properties
 └── test/
```

---

# Database Schema

## Main Tables

* users
* roles
* user_roles
* institutions
* certificates
* verification_logs
* activity_logs

---

# Entity Relationships

## Users ↔ Roles

* Many-to-Many relationship
* Managed through `user_roles`

## Institutions ↔ Certificates

* One Institution can issue many Certificates

## Certificates ↔ Verification Logs

* One Certificate can have many Verification Logs
* Each Verification Log belongs to one Certificate

---

# Installation

## Clone Repository

```bash
git clone https://github.com/your-username/your-repository.git
cd your-repository
```

---

# Configure Database

Update `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/certificate_db
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

# Run Application

## Using Maven

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

---

# API Base URL

```text
http://localhost:8080/api
```

---

# Authentication

The system uses JWT-based authentication.

## Example Login Request

```json
{
  "username": "admin",
  "password": "password"
}
```

---

# Certificate Verification Flow

1. Upload certificate
2. Extract certificate details
3. Verify certificate authenticity
4. Compare certificate hash
5. Generate verification result
6. Store verification logs

---

# Security Features

* Password hashing
* JWT authentication
* Role-based authorization
* Verification logging
* Activity tracking
* Risk score analysis

---

# Future Enhancements

* Blockchain-based certificate storage
* AI-powered tampering detection
* Email notifications
* Multi-institution support
* Cloud deployment
* Analytics dashboard

---

# Deployment

You can deploy the backend and frontend for free using:

* Zoho Catalyst
* Github Pages

---

# License

This project is developed for educational and research purposes.

---

