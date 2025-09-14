# Book Service

RESTful API for managing books. Provides endpoints to create a book and retrieve a list of books by author sorted by published date (descending).

This README covers:
- Database setup (schema creation)
- Running the server
- Running integration tests
- Example API requests and expected responses


## Prerequisites
- Java 21
- Maven 3.9+
- MySQL 8+ (for local/dev runtime)
  - Tests use H2 in-memory DB automatically; MySQL is not required to run the tests.


## Database Setup (MySQL)
1. Create a database and a user (or use your existing credentials). Example:
   ```sql
   CREATE DATABASE book CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   CREATE USER 'book_user'@'%' IDENTIFIED BY 'strong_password';
   GRANT ALL PRIVILEGES ON book.* TO 'book_user'@'%';
   FLUSH PRIVILEGES;
   ```
2. Configure connection in src/main/resources/application.properties (defaults shown below). You can also override via environment variables or JVM system properties.
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/book
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   
   # Ensure schema.sql and data.sql are executed on startup
   spring.sql.init.mode=always
   spring.sql.init.schema-locations=classpath:schema.sql
   spring.sql.init.data-locations=classpath:data.sql
   spring.jpa.hibernate.ddl-auto=none
   spring.sql.init.continue-on-error=true
   ```
3. Schema creation script
   - The app is configured to automatically run src/main/resources/schema.sql on startup. The schema script is:
     ```sql
     CREATE TABLE IF NOT EXISTS book
     (
         id             BIGINT       NOT NULL AUTO_INCREMENT,
         title          VARCHAR(255) NOT NULL,
         author         VARCHAR(255) NOT NULL,
         published_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         PRIMARY KEY (id),
         INDEX idx_author (author),
         INDEX idx_author_published_date (author, published_date)
     );
     ```
   - Optional: seed data can be placed in src/main/resources/data.sql (executed on startup when present).


## Build and Run the Server
You can run the application directly with Maven or from a packaged JAR.

- Run with Maven (dev):
  ```bash
  mvn spring-boot:run
  ```
  You can override DB settings on the command line, e.g.:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=jdbc:mysql://localhost:3306/book -Dspring.datasource.username=book_user -Dspring.datasource.password=strong_password"
  ```

- Package and run the JAR:
  ```bash
  mvn clean package
  java -jar target/book-0.0.1.jar
  ```

- Swagger/OpenAPI UI:
  Once the app is running, open:
  http://localhost:8080/swagger-ui.html


## Running Tests (including Integration Tests)
- All tests (unit and integration) run with:
  ```bash
  mvn test
  ```
- Integration tests use Spring Boot with a random port and H2 in-memory database under the `test` profile. The test configuration is in src/test/resources/application-test.properties and does not require MySQL.


## API Overview
Base path: `/api/v1/books`

Notes:
- Dates are strings in format `yyyy-MM-dd HH:mm:ss`.
- Validation: `publishedDate` year must be between 1000 and current year (CE) or between 1543 and currentYear+543 (BE). Invalid values return HTTP 400.
- The GET endpoint expects a JSON request body containing the author name (this is atypical for GET requests but is supported by this service).

### 1) Create a Book
- Method/URL: `POST /api/v1/books`
- Request body:
  ```json
  {
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "publisher": "Pearson",
    "publishedDate": "2020-01-01 10:00:00"
  }
  ```
- Successful response (200 OK):
  ```json
  {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "publishedDate": "2020-01-01T10:00:00"
  }
  ```
- Validation error (400 Bad Request) example when year is invalid:
  ```json
  {
    "timestamp": "...",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/v1/books"
  }
  ```

cURL example:
```bash
curl -X POST "http://localhost:8080/api/v1/books" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "publisher": "Pearson",
    "publishedDate": "2020-01-01 10:00:00"
  }'
```

### 2) Get Books by Author (sorted by publishedDate DESC, limit 10)
- Method/URL: `GET /api/v1/books`
- Request body:
  ```json
  { "author": "Author A" }
  ```
- Successful response (200 OK): array of books, newest first. Example:
  ```json
  [
    {
      "id": 2,
      "title": "T2",
      "author": "Author A",
      "publishedDate": "2024-05-02T12:00:00"
    },
    {
      "id": 1,
      "title": "T1",
      "author": "Author A",
      "publishedDate": "2024-05-01T12:00:00"
    }
  ]
  ```

cURL example (GET with JSON body):
```bash
curl -X GET "http://localhost:8080/api/v1/books" \
  -H "Content-Type: application/json" \
  --data '{ "author": "Author A" }'
```

Note: Some tools/servers discourage GET requests with a body. This service supports it as implemented. If your HTTP client cannot send a GET body, consider changing the request to a POST in your client or adjusting the controller accordingly.


## Troubleshooting
- If the application fails to start due to DB connectivity, verify MySQL is running and credentials in application.properties are correct.
- To use a different DB without changing files, pass JVM properties:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=jdbc:mysql://host:3306/book -Dspring.datasource.username=... -Dspring.datasource.password=..."
  ```
- For clean local DB state, you can drop and recreate the schema manually, or let schema.sql run on startup.