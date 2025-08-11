# Defsec Tasks CRUD (Spring Boot + MySQL)

Simple CRUD service for managing tasks.

## Prerequisites
- Java 17 (JDK)
  - macOS: see [macOS Setup](docs/SETUP_MACOS.md)
  - Ubuntu/Debian: see [Linux Setup](docs/SETUP_LINUX.md)
- Docker Desktop (Engine + Compose v2)
- Gradle Wrapper is included (`./gradlew`)

## Tech
- Spring Boot 3 (Web, Data JPA, Validation, Flyway)
- MySQL 8 (Dockerized)
- Flyway migrations in `src/main/resources/db/migration`
- Jackson formats timestamps as ISO-8601 UTC: `yyyy-MM-dd'T'HH:mm:ssZ`

## Configuration
- Database connection from app → MySQL service `db` via Docker network
- App config: `src/main/resources/application.properties`

### Environment Variables
All environment variables are optional with sensible defaults:

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | `yourpassword` | MySQL root password |
| `MYSQL_DATABASE` | `defsecdb` | MySQL database name |
| `MYSQL_USER` | `appuser` | MySQL application user (used by Spring Boot app) |
| `MYSQL_PASSWORD` | `apppassword` | MySQL application user password (used by Spring Boot app) |
 

## Run (Docker)
```bash
# from project root 
./gradlew all 
docker-compose up --build
```
- App: `http://localhost:8080`
- DB: MySQL on `localhost:3306` (inside compose use host `db`)

Stop:
```bash
docker-compose down
```
Reset DB (drop data):
```bash
docker-compose down -v
```

### Run clean build test bootJar - via one single command
```bash
./gradlew all
```


## API
Base URL: `http://localhost:8080`

**API Documentation**: `/docs` (Swagger UI)

| Method | Endpoint | Description | Notes |
|--------|----------|-------------|-------|
| GET | `/tasks` | List all tasks | Returns array of tasks |
| GET | `/tasks/{id}` | Get task by ID | Returns single task or 404 |
| POST | `/tasks` | Create new task | Do not include `id` in request body |
| PUT | `/tasks/{id}` | Update existing task | Do not include `id` in request body |
| DELETE | `/tasks/{id}` | Delete task | Returns 204 on success |

### Task model (Response)
```json
{
  "id": 1,                      // system generated
  "title": "My task",           // required, unique, <= 255 chars
  "description": "...",         // optional, <= 500 chars
  "status": "PENDING|COMPLETED",// default PENDING
  "createdAt": "2025-01-15T14:30:45Z",  // ISO-8601 UTC format, system generated.
  "updatedAt": "2025-01-15T14:35:20Z"   // ISO-8601 UTC format, omitted when null, system generated.
}
```

### Task request (POST/PUT body)
```json
{
  "title": "My task",           // required, unique, <= 255 chars
  "description": "...",         // optional, <= 500 chars
  "status": "PENDING"           // optional, must be "PENDING" or "COMPLETED", defaults to "PENDING"
}
```
**Note**: `id`, `createdAt`, and `updatedAt` are managed by the system and should not be included in request bodies.

**Status Values**: The `status` field accepts only two values:
- `"PENDING"` - Task is not yet completed (default)
- `"COMPLETED"` - Task has been finished

**✅ Timezone**: Timestamps use ISO-8601 format `yyyy-MM-dd'T'HH:mm:ssZ` where **Z indicates UTC timezone**. This eliminates timezone ambiguity.

## cURL examples
List:
```bash
curl -s http://localhost:8080/tasks
```
Get by id:
```bash
curl -s http://localhost:8080/tasks/1
```
Create:
```bash
curl -s -X POST http://localhost:8080/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Write docs",
    "description": "README and examples",
    "status": "PENDING"
  }'
```
Update:
```bash
curl -s -X PUT http://localhost:8080/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Write docs",
    "description": "Updated description",
    "status": "COMPLETED"
  }'
```
Delete:
```bash
curl -s -X DELETE http://localhost:8080/tasks/1 -i
```

## Validation & Errors
- Title: required, unique, <= 255
- Description: optional, <= 500
- Status: PENDING or COMPLETED

Error shape (examples):
```json
{ "title": "Title is required" }
```
```json
{ "error": "Conflict", "message": "A task with the title 'X' already exists", "field": "title" }
```

## Troubleshooting
- Port busy 8080/3306: stop other services using those ports
- DB keeps old data: use `docker-compose down -v` to drop data volume (or add a named volume for persistence)
- Migrations: Flyway runs on startup; increment version for changes

## Build & Test
```bash
./gradlew clean test
```
Gradle prints a summary with passed/failed counts.

### Run a specific test class
```bash
./gradlew test --tests "defsec.crud.controller.TaskControllerTest"
```

### Re-run only failed tests
```bash
./gradlew test --tests "*" --rerun-tasks
```

### More verbose output
```bash
./gradlew test --info
./gradlew test --debug
```


