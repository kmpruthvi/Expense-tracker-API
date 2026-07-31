# Smart Expense Tracker API

A REST API for managing personal expenses, built with Java 17 and Spring Boot.
Data is stored in memory (a thread-safe `ConcurrentHashMap`) — no database required,
so all data resets when the app restarts.

## What was built

- Add an expense (`title`, `amount`, `category`, `date`) — `id` is server-generated
- View all expenses
- Filter expenses by category (case-insensitive)
- Calculate total expenses — overall, and broken down by category
- Delete an expense by id
- Input validation (blank title/category, non-positive amount, missing date → `400`)
- Consistent error responses for validation failures and "not found" (`404`)
- **Bonus:** OpenAPI/Swagger docs at `/swagger-ui.html`

## Requirements

- Java 17+
- Maven 3.8+ (or use the included `mvnw` wrapper if you add one — this repo assumes
  a locally installed Maven; run `mvn -v` to confirm before starting)

## Install dependencies

```bash
mvn clean install -DskipTests
```

## Run the server

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

Swagger UI (interactive API docs): `http://localhost:8080/swagger-ui.html`

## Run the tests

```bash
mvn test
```

This runs:
- `ExpenseServiceTest` — unit tests for the business logic (in-memory repo, no mocks needed)
- `ExpenseControllerTest` — MockMvc tests hitting real HTTP endpoints end-to-end

## API Reference

### Add an expense
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Groceries","amount":1200.50,"category":"Food","date":"2026-07-15"}'
```

### View all expenses
```bash
curl http://localhost:8080/api/expenses
```

### Filter by category
```bash
curl "http://localhost:8080/api/expenses?category=Food"
```

### Totals — overall and by category
```bash
curl http://localhost:8080/api/expenses/total
```
```json
{
  "overallTotal": 1450.50,
  "byCategory": { "Food": 1200.50, "Transport": 250.00 }
}
```

Totals scoped to one category (still shows the full `byCategory` map for context):
```bash
curl "http://localhost:8080/api/expenses/total?category=Food"
```

### Delete an expense
```bash
curl -X DELETE http://localhost:8080/api/expenses/1
```
Returns `204 No Content` on success, `404` if the id doesn't exist.

## Project structure

```
expense-tracker-api/
  README.md
  AI_NOTES.md
  pom.xml
  src/main/java/com/expensetracker/
    ExpenseTrackerApplication.java
    controller/ExpenseController.java
    service/ExpenseService.java
    repository/ExpenseRepository.java   # in-memory store
    model/Expense.java
    dto/ExpenseRequest.java
    dto/TotalResponse.java
    exception/ExpenseNotFoundException.java
    exception/GlobalExceptionHandler.java
  src/test/java/com/expensetracker/
    service/ExpenseServiceTest.java
    controller/ExpenseControllerTest.java
```

## Design notes

- **Validation:** `ExpenseRequest` uses Bean Validation (`@NotBlank`, `@NotNull`,
  `@DecimalMin`) so bad input fails fast with a clear `400` and field-level errors,
  rather than a generic 500.
- **Totals endpoint:** one endpoint (`/api/expenses/total`) covers both "overall"
  and "by category" from the spec — `byCategory` always shows the full breakdown,
  and `overallTotal` narrows to a single category when `?category=` is passed.
- **Sorting:** `GET /api/expenses` returns results sorted by date (most recent first)
  for readability; this wasn't required but is a small, low-risk UX improvement.
- **No database:** per the assignment, data is in-memory only and does not
  survive a restart.
