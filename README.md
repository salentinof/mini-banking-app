# Mini Banking App

A simple banking application built with **Spring Boot**, designed to manage users, bank accounts, and transactions (deposits, withdrawals, and transfers).

## 🚀 Technologies Used
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL (or H2 for testing)
- Lombok

## 📌 Main Features
- User creation and management
- Bank account creation and management
- Deposit and withdrawal operations
- Money transfers between accounts
- Transaction history for each account
- Centralized error handling with a Global Exception Handler

## 📂 Project Structure
- `entity/` → JPA entities (User, Account, Transaction)
- `controller/` → REST API endpoints
- `service/` → business logic
- `repository/` → database access layer
- `dto/` → request/response models
- `exception/` → custom exceptions + global handler

