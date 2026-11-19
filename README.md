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

Requires Java 21 (Maven Wrapper included).

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
./mvnw test                   # run the tests
```

The API starts on `http://localhost:8080`, Swagger UI at `/swagger-ui/index.html`.
Set the `JWT_SECRET` environment variable (32+ characters) outside local development.

## Authentication flow

Registration and login are public; every other endpoint needs a JWT in the `Authorization` header. Tokens are valid for 1 hour.

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

## Error handling

All errors return the same JSON shape: `timestamp`, `status`, `error`, `path`.

| Status | When |
|---|---|
| 400 | Validation errors, malformed JSON, insufficient funds, transfer to the same account |
| 401 | Wrong credentials, missing or invalid token |
| 404 | User, account or endpoint not found |
| 409 | Email already registered, concurrent update on the same account |

## Design decisions

- **One endpoint and one DTO per operation.** Each request carries only the fields it needs, so validation is plain `@Valid` with no conditional rules. Internally every operation is stored in the same `transactions` table.
- **`BigDecimal` for money**, never `double`, to avoid rounding errors.
- **Balance snapshot on every transaction.** Each transaction stores `balanceAfter`, the account balance right after the operation, like a bank statement line.
- **`@Transactional` service layer.** A transfer debits one account and credits the other in a single transaction: if anything fails, nothing is saved.
- **Optimistic locking on accounts.** `Account` has a `@Version` field, so two concurrent operations on the same account can't overwrite each other: the second one fails with `409 Conflict` and can be retried. A test fires 10 parallel withdrawals and checks that the balance never goes negative.
- **Stateless JWT authentication.** No server-side session; passwords are stored as BCrypt hashes.
- **Centralized error handling** with a `@RestControllerAdvice`, so clients never see stack traces.

## Roadmap

- Restrict account operations to the account owner
- Transaction history endpoint with pagination
- PostgreSQL with Docker Compose and Flyway migrations

## Author

Francesco Salentino — personal backend practice project.
