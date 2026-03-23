# Instructions for candidates

This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

## Requirements
- JDK 17
- Docker

## Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator


## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

**Feel free to change the structure of the solution, use a different library etc.**


## Running the Project

### Local Development
```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Start the application
./gradlew bootRun
```

The API starts on `http://localhost:8090`

### Docker
```bash
docker-compose up
```

- Payment Gateway: http://localhost:8090
- Bank Simulator: http://localhost:8080
- Swagger API Docs: http://localhost:8090/swagger-ui/index.html

## Key Considerations & Assumptions

**Architecture**: Clean architecture (Controller → Service → Repository) for testability and maintainability.

**Storage**: The in-memory HashMap was retained as the memory store to persist payment information.

**Idempotency**: In-memory cache-based duplicate transaction detection logic to prevent duplicate transaction issue. The 'Idempotency-Key' key header would need to be provided in the request.
- Note that the implementation for the cache here has no expiry, in keeping the implementation simple.

**Retry/Async**: No retry or asynchronous logic was implemented. The implementation relies on the user to manually retry a failed or unsuccessful transaction.

**Validation**: Full validation rules were implemented in accordance to the business constraints.

**Exceptions**: Robust exception handling with proper HTTP status codes.

## Data Validation

All validation rules were implemented as prescribed. Here are a few points to note:

**Card expiry date**: Expiry dates must either be in the present or future, that is, card for the present month and year are valid

**Currency**: Only USD, GBP and EUR are supported.