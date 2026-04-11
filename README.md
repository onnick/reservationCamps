# reservationCamps

Spring Boot REST API for camp reservations.

## Domain
Entities:
- `AppUser` (role: `ADMIN` or `CUSTOMER`)
- `Camp`
- `CampSession` (capacity, date range)
- `Reservation` (status lifecycle)

Business rules (examples):
- session cannot be created in the past
- reservation cannot be created/confirmed/cancelled after session start
- reservation is unique per (user, session)
- confirm respects session capacity
- only `CREATED` can be confirmed; only `CONFIRMED` can be paid; `PAID` cannot be cancelled

## Run Locally
Requirements: Java 21+, Docker (optional but recommended).

Run with Docker Compose (app + PostgreSQL):
```bash
docker compose up --build
```

Run directly (needs PostgreSQL running):
```bash
export DB_URL=jdbc:postgresql://localhost:5432/reservationcamps
export DB_USERNAME=reservationcamps
export DB_PASSWORD=reservationcamps
mvn -B spring-boot:run
```

Run without PostgreSQL (in-memory H2, profile `local`, port 8081):
```bash
mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

Health:
```bash
curl -fsS http://localhost:8080/actuator/health
```

Local profile health:
```bash
curl -fsS http://localhost:8081/actuator/health
```

## API
Admin endpoints require header `X-Actor-Role: ADMIN`.

Create user:
```bash
curl -sS -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@example.com"}'
```

Create camp:
```bash
curl -sS -X POST http://localhost:8080/api/camps \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Role: ADMIN' \
  -d '{"name":"Camp","basePriceCents":1000}'
```

## Architecture
The application follows a simple layered architecture:
- API layer: REST controllers in `src/main/java/.../api` expose HTTP endpoints and map requests/responses.
- Application/domain logic: services in `src/main/java/.../service` implement business rules (state transitions, capacity checks, role checks, duplicate prevention).
- Persistence: JPA entities in `src/main/java/.../domain` and Spring Data repositories in `src/main/java/.../domain/repo` store data in a relational database.
- Migrations: Flyway migrations in `src/main/resources/db/migration` (and `db/migration/h2` for the `local` profile) keep schema versioned.
- Error handling: `ApiExceptionHandler` converts domain exceptions to consistent HTTP responses (`403/404/409` + `ApiError`); request body validation errors are `400`.
- Actor/role resolution: `ActorResolver` reads request headers (`X-Actor-Role`, optional `X-Actor-Id`) to authorize admin-only operations.
- UI: a minimal static page is served from `src/main/resources/static/index.html` as a lightweight client for the API (admin/diagnostics are intentionally hidden).

## Tests
Unit tests: `*Test.java` (domain rules and controllers).

Integration tests: `*IT.java` using Testcontainers when Docker is available.

Run:
```bash
mvn -B verify
```

## Testing Strategy
The goal is to keep most business rules covered by fast unit tests and use a small number of realistic integration tests:
- Unit tests (`*Test.java`): focus on business rules and edge cases in services (e.g. invalid state transitions, capacity full, duplicates) and on controller wiring (status codes, JSON shape, header-based actor resolution).
- Integration tests (`*IT.java`): verify end-to-end behavior across layers (controller -> service -> DB) using a real PostgreSQL database via Testcontainers.
- Mocks/test doubles: Mockito is used for repository ports and for `NotificationPort` (external side effect). `Clock.fixed(...)` is used to make time-based rules deterministic.
- BDD: not used in this project; API flows are verified via integration tests instead.
- What is intentionally not tested: pure plumbing/boilerplate such as JPA annotations mapping details, simple DTO records, and the static UI HTML (the UI is a thin client over already-tested API rules).

## CI/CD
GitHub Actions:
- build + unit/integration tests
- JaCoCo coverage check + report artifact
- Checkstyle
- build + push Docker image to GHCR on `main`
- deploy to Kubernetes `staging` on `main` (kind on runner) + smoke test

## Kubernetes
Manifests use Kustomize overlays:
- staging: `k8s/overlays/staging`
- prod: `k8s/overlays/prod`

Secrets are not committed. Create `secret.env` based on `secret.env.example` and apply:
```bash
kubectl apply -k k8s/overlays/staging
kubectl -n staging port-forward svc/reservationcamps 8080:80
```
