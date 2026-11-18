# Mini Banking App

![CI](https://github.com/salentinof/mini-banking-app/actions/workflows/ci.yml/badge.svg)

A REST API for a simple bank: users register, log in with JWT, open accounts and move money with deposits, withdrawals and transfers.

## Tech stack

- Java 21, Spring Boot 3.5
- Spring Web, Spring Data JPA (Hibernate), Bean Validation
- Spring Security with stateless JWT authentication (jjwt), BCrypt password hashing
- H2 in-memory database
- MapStruct, Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc
- GitHub Actions CI

## Run locally

Requirements: Java 21. Maven is not needed, the project ships with the Maven Wrapper.

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:bankdb`)

Run the tests:

```bash
./mvnw test
```

## Authentication flow

Registration and login are public; every other endpoint needs a JWT in the `Authorization` header.

```bash
# 1. Register
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Mario","email":"mario@test.com","password":"secret"}'

# 2. Log in (the "username" field holds the email) and copy the token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"mario@test.com","password":"secret"}'

# 3. Call protected endpoints with the token
curl http://localhost:8080/accounts -H "Authorization: Bearer <token>"
```

Tokens are valid for 1 hour.

## API endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/users` | public | Register a user |
| POST | `/auth/login` | public | Log in and get a JWT |
| GET | `/users` | JWT | List users |
| GET | `/users/{id}` | JWT | Get a user |
| POST | `/accounts` | JWT | Open an account `{ "userId": 1 }` |
| GET | `/accounts` | JWT | List accounts |
| GET | `/accounts/{accountNumber}` | JWT | Get an account |
| POST | `/transactions/deposit` | JWT | `{ "accountNumber", "amount" }` |
| POST | `/transactions/withdraw` | JWT | `{ "accountNumber", "amount" }` |
| POST | `/transactions/transfer` | JWT | `{ "fromAccount", "toAccount", "amount" }` |

Example transfer:

```bash
curl -X POST http://localhost:8080/transactions/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"fromAccount":"ACC-123","toAccount":"ACC-456","amount":20}'
```

## Error handling

All errors share the same JSON shape:

```json
{
  "timestamp": "2026-11-18T15:17:48",
  "status": 400,
  "error": "Insufficient funds | account = ACC-123 | balance = 10.00 | request = 50",
  "path": "/transactions/withdraw"
}
```

| Status | When |
|---|---|
| 400 | Validation errors, malformed JSON, insufficient funds, transfer to the same account |
| 401 | Wrong credentials or invalid token |
| 404 | User, account or endpoint not found |
| 409 | Email already registered |

## Design decisions

- **One endpoint and one DTO per operation.** Each request carries only the fields it needs, so validation is plain `@Valid` with no conditional rules. Internally every operation is stored in the same `transactions` table.
- **`BigDecimal` for money**, never `double`, to avoid rounding errors.
- **`@Transactional` service layer.** A transfer debits one account and credits the other in a single transaction: if anything fails, nothing is saved.
- **Stateless JWT authentication.** No server-side session; passwords are stored as BCrypt hashes.
- **Centralized error handling** with a `@RestControllerAdvice`, so clients never see stack traces.

## Project structure

```
config/      Spring Security configuration
controller/  REST endpoints
service/     Business logic, JWT handling
repository/  Spring Data JPA repositories
entity/      JPA entities (User, Account, Transaction)
dto/         Request and response models
mapper/      MapStruct mappers
exception/   Custom exceptions and global handler
```

## Roadmap

- Restrict account operations to the account owner
- Record the balance after each transaction
- Transaction history endpoint with pagination
- PostgreSQL with Docker Compose and Flyway migrations
- Optimistic locking on account balances

## Author

Francesco Salentino — personal backend practice project.
